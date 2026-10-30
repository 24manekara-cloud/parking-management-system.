package com.parking.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Response DTO for an active booking, including live parking duration and estimated fee.
 */
public record ActiveBookingResponse(
        Long            bookingId,
        String          vehicleNumber,
        String          slotNumber,
        LocalDateTime   entryTime,
        String          parkingDuration,
        BigDecimal      estimatedFee
) {}
