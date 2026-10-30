package com.parking.exception;

/**
 * Thrown when a booking attempt targets a slot that is already occupied.
 * Maps to HTTP 409 Conflict.
 */
public class SlotUnavailableException extends RuntimeException {

    public SlotUnavailableException(String slotNumber) {
        super("Parking slot '" + slotNumber + "' is already occupied. Please choose a different slot.");
    }

    public SlotUnavailableException(Long slotId) {
        super("Parking slot with ID " + slotId + " is currently unavailable.");
    }
}
