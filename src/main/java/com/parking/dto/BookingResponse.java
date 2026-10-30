package com.parking.dto;

import com.parking.model.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Response DTO for a booking (creation or retrieval).
 */
public record BookingResponse(
        Long            bookingId,
        String          vehicleNumber,
        Long            slotId,
        String          slotNumber,
        LocalDateTime   entryTime,
        LocalDateTime   exitTime,
        BigDecimal      amount,
        PaymentStatus   paymentStatus
) {}
