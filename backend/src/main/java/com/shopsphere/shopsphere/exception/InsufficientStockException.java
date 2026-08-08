package com.shopsphere.shopsphere.exception;

/**
 * Thrown when trying to add-to-cart / checkout more units than are in stock.
 * Mapped to HTTP 400 Bad Request.
 */
public class InsufficientStockException extends RuntimeException {
    public InsufficientStockException(String message) {
        super(message);
    }
}
