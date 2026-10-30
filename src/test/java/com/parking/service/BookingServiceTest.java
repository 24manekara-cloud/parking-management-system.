package com.parking.service;

import com.parking.dto.ActiveBookingResponse;
import com.parking.dto.BookingRequest;
import com.parking.dto.BookingResponse;
import com.parking.dto.CheckoutResponse;
import com.parking.exception.BookingAlreadyCompletedException;
import com.parking.exception.InvalidBookingException;
import com.parking.exception.ResourceNotFoundException;
import com.parking.exception.SlotUnavailableException;
import com.parking.model.Booking;
import com.parking.model.PaymentStatus;
import com.parking.model.ParkingSlot;
import com.parking.model.SlotStatus;
import com.parking.repository.BookingRepository;
import com.parking.repository.ParkingSlotRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link BookingService}.
 * All external dependencies are mocked with Mockito.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("BookingService — Service Layer Tests")
class BookingServiceTest {

    @Mock private BookingRepository     bookingRepository;
    @Mock private ParkingSlotRepository slotRepository;

    // BillingService is NOT mocked — we use the real implementation
    private BillingService billingService;

    @InjectMocks
    private BookingService bookingService;

    private ParkingSlot availableSlot;
    private ParkingSlot occupiedSlot;
    private Booking     activeBooking;
    private Booking     completedBooking;

    @BeforeEach
    void setUp() {
        billingService = new BillingService();

        // Rebuild BookingService with real BillingService injected
        bookingService = new BookingService(bookingRepository, slotRepository, billingService);

        availableSlot = ParkingSlot.builder()
                .id(1L)
                .slotNumber("A-01")
                .status(SlotStatus.AVAILABLE)
                .build();

        occupiedSlot = ParkingSlot.builder()
                .id(2L)
                .slotNumber("A-02")
                .status(SlotStatus.OCCUPIED)
                .build();

        activeBooking = Booking.builder()
                .id(100L)
                .vehicleNumber("MH31AB1234")
                .slot(availableSlot)
                .entryTime(LocalDateTime.now().minusHours(1))
                .paymentStatus(PaymentStatus.PENDING)
                .build();

        completedBooking = Booking.builder()
                .id(200L)
                .vehicleNumber("DL01CD5678")
                .slot(availableSlot)
                .entryTime(LocalDateTime.now().minusHours(2))
                .exitTime(LocalDateTime.now().minusMinutes(10))
                .paymentStatus(PaymentStatus.PAID)
                .build();
    }

    // ── Create Booking Tests ─────────────────────────────────────────────────

    @Test
    @DisplayName("Successful booking — slot becomes OCCUPIED, booking persisted")
    void createBooking_success() {
        BookingRequest request = new BookingRequest("MH31AB1234", 1L);

        when(slotRepository.findByIdWithLock(1L)).thenReturn(Optional.of(availableSlot));
        when(bookingRepository.existsActiveBookingForVehicle("MH31AB1234")).thenReturn(false);
        when(slotRepository.save(any(ParkingSlot.class))).thenReturn(availableSlot);
        when(bookingRepository.save(any(Booking.class))).thenReturn(activeBooking);

        BookingResponse response = bookingService.createBooking(request);

        assertThat(response).isNotNull();
        assertThat(response.vehicleNumber()).isEqualTo("MH31AB1234");
        assertThat(response.paymentStatus()).isEqualTo(PaymentStatus.PENDING);

        verify(slotRepository).findByIdWithLock(1L);
        verify(slotRepository).save(any(ParkingSlot.class));
        verify(bookingRepository).save(any(Booking.class));
    }

    @Test
    @DisplayName("Book an occupied slot → SlotUnavailableException")
    void createBooking_occupiedSlot_throwsSlotUnavailableException() {
        BookingRequest request = new BookingRequest("MH31AB1234", 2L);

        when(slotRepository.findByIdWithLock(2L)).thenReturn(Optional.of(occupiedSlot));

        assertThatThrownBy(() -> bookingService.createBooking(request))
                .isInstanceOf(SlotUnavailableException.class)
                .hasMessageContaining("A-02");

        verify(bookingRepository, never()).save(any());
    }

    @Test
    @DisplayName("Book a nonexistent slot → ResourceNotFoundException")
    void createBooking_slotNotFound_throwsResourceNotFoundException() {
        BookingRequest request = new BookingRequest("MH31AB1234", 99L);

        when(slotRepository.findByIdWithLock(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.createBooking(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");

        verify(bookingRepository, never()).save(any());
    }

    @Test
    @DisplayName("Vehicle already has an active booking → InvalidBookingException")
    void createBooking_vehicleAlreadyBooked_throwsInvalidBookingException() {
        BookingRequest request = new BookingRequest("MH31AB1234", 1L);

        when(slotRepository.findByIdWithLock(1L)).thenReturn(Optional.of(availableSlot));
        when(bookingRepository.existsActiveBookingForVehicle("MH31AB1234")).thenReturn(true);

        assertThatThrownBy(() -> bookingService.createBooking(request))
                .isInstanceOf(InvalidBookingException.class)
                .hasMessageContaining("MH31AB1234");

        verify(bookingRepository, never()).save(any());
    }

    // ── Checkout Tests ───────────────────────────────────────────────────────

    @Test
    @DisplayName("Successful checkout — slot released, fee calculated, status PAID")
    void checkout_success() {
        when(bookingRepository.findById(100L)).thenReturn(Optional.of(activeBooking));
        when(slotRepository.save(any(ParkingSlot.class))).thenReturn(availableSlot);
        when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> inv.getArgument(0));

        CheckoutResponse response = bookingService.checkout(100L);

        assertThat(response).isNotNull();
        assertThat(response.paymentStatus()).isEqualTo(PaymentStatus.PAID);
        assertThat(response.exitTime()).isNotNull();
        assertThat(response.totalAmount()).isNotNull().isPositive();
        assertThat(response.message()).contains("A-01");

        verify(slotRepository).save(argThat(s -> s.getStatus() == SlotStatus.AVAILABLE));
    }

    @Test
    @DisplayName("Checkout of nonexistent booking → ResourceNotFoundException")
    void checkout_notFound_throwsResourceNotFoundException() {
        when(bookingRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.checkout(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("999");
    }

    @Test
    @DisplayName("Double checkout → BookingAlreadyCompletedException")
    void checkout_alreadyCompleted_throwsBookingAlreadyCompletedException() {
        when(bookingRepository.findById(200L)).thenReturn(Optional.of(completedBooking));

        assertThatThrownBy(() -> bookingService.checkout(200L))
                .isInstanceOf(BookingAlreadyCompletedException.class)
                .hasMessageContaining("200");

        verify(bookingRepository, never()).save(any());
    }

    // ── Active Bookings Tests ────────────────────────────────────────────────

    @Test
    @DisplayName("Get active bookings — returns live duration and estimated fee")
    void getActiveBookings_returnsLiveData() {
        when(bookingRepository.findAllActiveBookings()).thenReturn(List.of(activeBooking));

        List<ActiveBookingResponse> result = bookingService.getActiveBookings();

        assertThat(result).hasSize(1);
        ActiveBookingResponse resp = result.get(0);
        assertThat(resp.vehicleNumber()).isEqualTo("MH31AB1234");
        assertThat(resp.slotNumber()).isEqualTo("A-01");
        assertThat(resp.estimatedFee()).isNotNull();
        assertThat(resp.parkingDuration()).isNotBlank();
    }

    @Test
    @DisplayName("Get active bookings — returns empty list when no active bookings")
    void getActiveBookings_empty() {
        when(bookingRepository.findAllActiveBookings()).thenReturn(List.of());

        List<ActiveBookingResponse> result = bookingService.getActiveBookings();

        assertThat(result).isEmpty();
    }
}
