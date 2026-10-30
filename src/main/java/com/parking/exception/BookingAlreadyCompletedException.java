package com.parking.exception;

/**
 * Thrown when a checkout is attempted for a booking that has already been completed.
 * Maps to HTTP 409 Conflict.
 */
public class BookingAlreadyCompletedException extends RuntimeException {

    public BookingAlreadyCompletedException(Long bookingId) {
        super("Booking with ID " + bookingId + " has already been checked out.");
    }
}
