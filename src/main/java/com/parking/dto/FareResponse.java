package com.parking.dto;

import java.math.BigDecimal;

/**
 * Response DTO for a real-time fare estimate (does not alter the booking).
 */
public record FareResponse(
        Long       bookingId,
        String     vehicleNumber,
        String     slotNumber,
        String     currentDuration,
        BigDecimal estimatedFare
) {}
