package com.shopsphere.shopsphere.exception;

/**
 * Thrown when a lookup by id/email/etc. finds nothing.
 * Mapped to HTTP 404 in GlobalExceptionHandler.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public static ResourceNotFoundException of(String entity, Object id) {
        return new ResourceNotFoundException(entity + " not found with id: " + id);
    }
}
