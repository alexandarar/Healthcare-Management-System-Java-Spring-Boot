package com.hms.exception;

/** Thrown for double-bookings, past-dated slots, or doctor-unavailable windows. */
public class InvalidAppointmentException extends RuntimeException {
    public InvalidAppointmentException(String message) {
        super(message);
    }
}
