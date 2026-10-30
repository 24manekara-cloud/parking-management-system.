package com.parking.service;

import com.parking.exception.InvalidBookingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Calculates parking fees based on the following policy:
 * <pre>
 *   ₹50 per every 3 hours (or fraction thereof).
 *   Fee = CEILING(durationSeconds / 10800) × 50
 *   Special case: 0 seconds → ₹0
 * </pre>
 *
 * <p>This class is intentionally kept free of Spring Data / JPA dependencies
 * so it can be unit-tested without a full application context.</p>
 */
@Service
public class BillingService {

    private static final Logger log = LoggerFactory.getLogger(BillingService.class);

    /** Rate charged per billing interval (₹50). */
    public static final BigDecimal RATE_PER_INTERVAL = new BigDecimal("50");

    /** Billing interval in seconds (3 hours = 10 800 s). */
    public static final long INTERVAL_SECONDS = 10_800L;

    /**
     * Calculates the parking fee for a completed or estimated stay.
     *
     * @param entryTime the moment the vehicle entered (non-null)
     * @param exitTime  the moment the vehicle exited or the current time (non-null)
     * @return the fee as a non-null {@link BigDecimal}
     * @throws InvalidBookingException if either timestamp is null,
     *                                  or exitTime is before entryTime
     */
    public BigDecimal calculateFee(LocalDateTime entryTime, LocalDateTime exitTime) {
        validateTimestamps(entryTime, exitTime);

        long durationSeconds = Duration.between(entryTime, exitTime).getSeconds();

        if (durationSeconds < 0) {
            // validateTimestamps already blocks this path, but be defensive
            throw new InvalidBookingException(
                    "Exit time cannot be before entry time. Entry: " + entryTime + ", Exit: " + exitTime);
        }

        if (durationSeconds == 0) {
            log.debug("Zero-duration stay — fee is ₹0");
            return BigDecimal.ZERO;
        }

        // CEILING(durationSeconds / INTERVAL_SECONDS)
        long intervals = (durationSeconds + INTERVAL_SECONDS - 1) / INTERVAL_SECONDS;
        BigDecimal fee = RATE_PER_INTERVAL.multiply(BigDecimal.valueOf(intervals));

        log.debug("Duration {}s → {} intervals → fee ₹{}", durationSeconds, intervals, fee);
        return fee;
    }

    /**
     * Formats a duration (in seconds) into a human-readable string.
     * Example: 7320 seconds → "2h 2m 0s"
     *
     * @param entryTime start of the period
     * @param refTime   reference time (exit or current time)
     * @return formatted duration string
     */
    public String formatDuration(LocalDateTime entryTime, LocalDateTime refTime) {
        if (entryTime == null || refTime == null) {
            return "N/A";
        }
        Duration d = Duration.between(entryTime, refTime);
        if (d.isNegative()) {
            return "Invalid";
        }
        long hours   = d.toHours();
        long minutes = d.toMinutesPart();
        long seconds = d.toSecondsPart();
        return String.format("%dh %dm %ds", hours, minutes, seconds);
    }

    // ── Private helpers ──────────────────────────────────────────────────────

    private void validateTimestamps(LocalDateTime entryTime, LocalDateTime exitTime) {
        if (entryTime == null) {
            throw new InvalidBookingException("Entry time must not be null.");
        }
        if (exitTime == null) {
            throw new InvalidBookingException("Exit time must not be null.");
        }
        if (exitTime.isBefore(entryTime)) {
            throw new InvalidBookingException(
                    "Exit time (" + exitTime + ") cannot be before entry time (" + entryTime + ").");
        }
    }
}
