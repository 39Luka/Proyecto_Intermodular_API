package com.bakery.bakeryapi.product.exception;

import com.bakery.bakeryapi.shared.exception.ApiException;
import org.springframework.http.HttpStatus;

/**
 * Raised when a purchase requests more units than the product has in stock.
 */
public class InsufficientStockException extends ApiException {

    public InsufficientStockException(Long productId, int available, int requested) {
        super(
                "Insufficient stock for product '" + productId + "'. Available: " + available + ", requested: " + requested,
                HttpStatus.CONFLICT
        );
    }
}


