package com.parking.dto;

import com.parking.model.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Response DTO returned after a vehicle checks out.
 * Contains the final billing details and slot release confirmation.
 */
public record CheckoutResponse(
        Long            bookingId,
        String          vehicleNumber,
        String          slotNumber,
        LocalDateTime   entryTime,
        LocalDateTime   exitTime,
        String          totalDuration,
        BigDecimal      totalAmount,
        PaymentStatus   paymentStatus,
        String          message
) {}
