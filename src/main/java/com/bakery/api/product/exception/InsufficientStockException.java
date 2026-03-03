package com.bakery.api.product.exception;

import com.bakery.api.common.exception.ApiException;
import org.springframework.http.HttpStatus;

public class InsufficientStockException extends ApiException {

    public InsufficientStockException(Long productId, int available, int requested) {
        super(
                "Insufficient stock for product '" + productId + "'. Available: " + available + ", requested: " + requested,
                HttpStatus.CONFLICT
        );
    }
}


