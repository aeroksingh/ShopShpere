package com.shopsphere.shopsphere.exception;

/**
 * Thrown on unique-constraint style conflicts (e.g. email already registered).
 * Mapped to HTTP 409 Conflict.
 */
public class DuplicateResourceException extends RuntimeException {
    public DuplicateResourceException(String message) {
        super(message);
    }
}
