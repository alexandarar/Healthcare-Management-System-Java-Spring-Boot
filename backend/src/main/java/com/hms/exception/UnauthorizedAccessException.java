package com.hms.exception;

/** Thrown when an authenticated user tries to access a resource that isn't theirs
 *  (e.g. one patient requesting another patient's medical record). */
public class UnauthorizedAccessException extends RuntimeException {
    public UnauthorizedAccessException(String message) {
        super(message);
    }
}
