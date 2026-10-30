package com.parking.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;

/**
 * Request DTO for creating a new booking.
 */
public record BookingRequest(

        @NotBlank(message = "Vehicle number must not be blank")
        @Pattern(
            regexp = "^[A-Z]{2}[0-9]{1,2}[A-Z]{1,3}[0-9]{4}$",
            message = "Vehicle number must follow Indian registration format (e.g. MH31AB1234)"
        )
        String vehicleNumber,

        @NotNull(message = "Slot ID must not be null")
        @Positive(message = "Slot ID must be a positive number")
        Long slotId
) {}
