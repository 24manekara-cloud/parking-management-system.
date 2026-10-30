package com.parking.service;

import com.parking.dto.*;
import com.parking.exception.BookingAlreadyCompletedException;
import com.parking.exception.InvalidBookingException;
import com.parking.exception.ResourceNotFoundException;
import com.parking.exception.SlotUnavailableException;
import com.parking.model.Booking;
import com.parking.model.PaymentStatus;
import com.parking.model.ParkingSlot;
import com.parking.repository.BookingRepository;
import com.parking.repository.ParkingSlotRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Core business logic for creating bookings, handling checkouts,
 * retrieving active bookings, and computing live fare estimates.
 */
@Service
@Transactional(readOnly = true)
public class BookingService {

    private static final Logger log = LoggerFactory.getLogger(BookingService.class);

    private final BookingRepository    bookingRepository;
    private final ParkingSlotRepository slotRepository;
    private final BillingService        billingService;

    public BookingService(
            BookingRepository    bookingRepository,
            ParkingSlotRepository slotRepository,
            BillingService        billingService) {
        this.bookingRepository = bookingRepository;
        this.slotRepository    = slotRepository;
        this.billingService    = billingService;
    }

    // ── Create Booking ───────────────────────────────────────────────────────

    /**
     * Books a parking slot for a vehicle.
     *
     * <p>A pessimistic write lock is acquired on the slot row to eliminate
     * race conditions when multiple threads attempt to book the same slot
     * simultaneously.</p>
     *
     * @param request validated booking request
     * @return the persisted booking as a {@link BookingResponse}
     * @throws ResourceNotFoundException if the slot does not exist
     * @throws SlotUnavailableException  if the slot is currently occupied
     * @throws InvalidBookingException   if the vehicle already has an active booking
     */
    @Transactional
    public BookingResponse createBooking(BookingRequest request) {
        log.info("Creating booking for vehicle {} at slot ID {}",
                request.vehicleNumber(), request.slotId());

        // Acquire pessimistic lock before reading status — prevents double-booking
        ParkingSlot slot = slotRepository.findByIdWithLock(request.slotId())
                .orElseThrow(() -> new ResourceNotFoundException("ParkingSlot", request.slotId()));

        if (!slot.isAvailable()) {
            throw new SlotUnavailableException(slot.getSlotNumber());
        }

        // Guard: one vehicle → one active booking at a time
        if (bookingRepository.existsActiveBookingForVehicle(request.vehicleNumber())) {
            throw new InvalidBookingException(
                    "Vehicle " + request.vehicleNumber() + " already has an active booking. "
                    + "Please checkout before creating a new one.");
        }

        // Mark slot as occupied
        slot.occupy();
        slotRepository.save(slot);

        // Persist the booking
        Booking booking = Booking.builder()
                .vehicleNumber(request.vehicleNumber())
                .slot(slot)
                .entryTime(LocalDateTime.now())
                .paymentStatus(PaymentStatus.PENDING)
                .build();

        Booking saved = bookingRepository.save(booking);
        log.info("Booking created with ID {} for vehicle {}", saved.getId(), saved.getVehicleNumber());

        return toBookingResponse(saved);
    }

    // ── Active Bookings ──────────────────────────────────────────────────────

    /**
     * Returns all currently active (not-checked-out) bookings with
     * live duration and estimated fee included.
     */
    public List<ActiveBookingResponse> getActiveBookings() {
        log.debug("Fetching all active bookings");
        LocalDateTime now = LocalDateTime.now();

        return bookingRepository.findAllActiveBookings()
                .stream()
                .map(b -> toActiveBookingResponse(b, now))
                .toList();
    }

    // ── Checkout ─────────────────────────────────────────────────────────────

    /**
     * Performs checkout for the given booking.
     * <ul>
     *   <li>Sets exit time and calculates the fee.</li>
     *   <li>Marks payment status as PAID.</li>
     *   <li>Releases the parking slot.</li>
     * </ul>
     *
     * @param bookingId the ID of the booking to checkout
     * @return a {@link CheckoutResponse} with full billing details
     * @throws ResourceNotFoundException        if no booking matches the ID
     * @throws BookingAlreadyCompletedException  if the booking is already checked out
     */
    @Transactional
    public CheckoutResponse checkout(Long bookingId) {
        log.info("Processing checkout for booking ID {}", bookingId);

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", bookingId));

        if (booking.isCheckedOut()) {
            throw new BookingAlreadyCompletedException(bookingId);
        }

        LocalDateTime exitTime = LocalDateTime.now();
        BigDecimal fee = billingService.calculateFee(booking.getEntryTime(), exitTime);

        booking.setExitTime(exitTime);
        booking.setAmount(fee);
        booking.setPaymentStatus(PaymentStatus.PAID);

        // Release the slot
        ParkingSlot slot = booking.getSlot();
        slot.release();
        slotRepository.save(slot);

        Booking saved = bookingRepository.save(booking);
        log.info("Checkout complete for booking ID {}. Fee: ₹{}", bookingId, fee);

        return toCheckoutResponse(saved);
    }

    // ── Live Fare Estimate ───────────────────────────────────────────────────

    /**
     * Returns the current estimated fare for an active booking without
     * modifying the booking or releasing the slot.
     *
     * @param bookingId the ID of the active booking
     * @return a {@link FareResponse} with the live estimate
     * @throws ResourceNotFoundException if no booking matches the ID
     * @throws BookingAlreadyCompletedException if the booking is already checked out
     */
    public FareResponse getLiveFare(Long bookingId) {
        log.debug("Computing live fare for booking ID {}", bookingId);

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", bookingId));

        if (booking.isCheckedOut()) {
            throw new BookingAlreadyCompletedException(bookingId);
        }

        LocalDateTime now = LocalDateTime.now();
        BigDecimal estimatedFare = billingService.calculateFee(booking.getEntryTime(), now);
        String duration = billingService.formatDuration(booking.getEntryTime(), now);

        return new FareResponse(
                booking.getId(),
                booking.getVehicleNumber(),
                booking.getSlot().getSlotNumber(),
                duration,
                estimatedFare
        );
    }

    // ── Mappers ──────────────────────────────────────────────────────────────

    private BookingResponse toBookingResponse(Booking b) {
        return new BookingResponse(
                b.getId(),
                b.getVehicleNumber(),
                b.getSlot().getId(),
                b.getSlot().getSlotNumber(),
                b.getEntryTime(),
                b.getExitTime(),
                b.getAmount(),
                b.getPaymentStatus()
        );
    }

    private ActiveBookingResponse toActiveBookingResponse(Booking b, LocalDateTime now) {
        BigDecimal estimatedFee = billingService.calculateFee(b.getEntryTime(), now);
        String duration = billingService.formatDuration(b.getEntryTime(), now);

        return new ActiveBookingResponse(
                b.getId(),
                b.getVehicleNumber(),
                b.getSlot().getSlotNumber(),
                b.getEntryTime(),
                duration,
                estimatedFee
        );
    }

    private CheckoutResponse toCheckoutResponse(Booking b) {
        String duration = billingService.formatDuration(b.getEntryTime(), b.getExitTime());
        return new CheckoutResponse(
                b.getId(),
                b.getVehicleNumber(),
                b.getSlot().getSlotNumber(),
                b.getEntryTime(),
                b.getExitTime(),
                duration,
                b.getAmount(),
                b.getPaymentStatus(),
                "Checkout successful. Slot " + b.getSlot().getSlotNumber() + " is now available."
        );
    }
}
