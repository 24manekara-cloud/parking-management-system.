package com.parking.exception;

/**
 * Thrown when a booking operation contains invalid data or violates business rules.
 * Maps to HTTP 400 Bad Request.
 */
public class InvalidBookingException extends RuntimeException {

    public InvalidBookingException(String message) {
        super(message);
    }
}
