package com.shopsphere.shopsphere.exception;

/** Generic 400 for business-rule violations that don't fit a more specific exception. */
public class BadRequestException extends RuntimeException {
    public BadRequestException(String message) {
        super(message);
    }
}
