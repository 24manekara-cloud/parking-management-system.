package com.parking.repository;

import com.parking.model.Booking;
import com.parking.model.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Spring Data JPA repository for {@link Booking} entities.
 */
@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    /**
     * Returns all bookings that have not yet been checked out (exitTime is null).
     * Uses a JOIN FETCH to load the associated slot in a single query.
     */
    @Query("SELECT b FROM Booking b JOIN FETCH b.slot WHERE b.exitTime IS NULL")
    List<Booking> findAllActiveBookings();

    /**
     * Checks whether any active (not-checked-out) booking exists for the given slot.
     * Used to prevent double-booking of the same slot.
     */
    @Query("SELECT COUNT(b) > 0 FROM Booking b WHERE b.slot.id = :slotId AND b.exitTime IS NULL")
    boolean existsActiveBookingForSlot(@Param("slotId") Long slotId);

    /**
     * Checks whether any active booking exists for the given vehicle number.
     * Used to prevent a vehicle from holding multiple active bookings.
     */
    @Query("SELECT COUNT(b) > 0 FROM Booking b WHERE b.vehicleNumber = :vehicleNumber AND b.exitTime IS NULL")
    boolean existsActiveBookingForVehicle(@Param("vehicleNumber") String vehicleNumber);

    /**
     * Aggregates total revenue for a specific calendar date using a DB-side SUM.
     * Only counts bookings that are PAID and whose exit_time falls on the given date.
     *
     * @param startOfDay  00:00:00 of the target date
     * @param endOfDay    23:59:59.999... of the target date
     * @param status      must be {@link PaymentStatus#PAID}
     * @return total revenue as BigDecimal, or null if no bookings match
     */
    @Query("""
           SELECT COALESCE(SUM(b.amount), 0)
           FROM Booking b
           WHERE b.exitTime >= :startOfDay
             AND b.exitTime < :endOfDay
             AND b.paymentStatus = :status
           """)
    BigDecimal sumRevenueByDateAndStatus(
            @Param("startOfDay")  LocalDateTime startOfDay,
            @Param("endOfDay")    LocalDateTime endOfDay,
            @Param("status")      PaymentStatus status
    );
}
