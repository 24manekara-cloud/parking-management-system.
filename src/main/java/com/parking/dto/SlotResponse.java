package com.parking.dto;

import com.parking.model.SlotStatus;

/**
 * Response DTO for a parking slot.
 */
public record SlotResponse(
        Long       id,
        String     slotNumber,
        SlotStatus status
) {}
