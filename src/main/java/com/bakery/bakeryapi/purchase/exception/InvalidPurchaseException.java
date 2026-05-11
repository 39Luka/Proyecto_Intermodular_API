package com.bakery.bakeryapi.purchase.exception;

import com.bakery.bakeryapi.shared.exception.ApiException;
import org.springframework.http.HttpStatus;

/**
 * Raised when a purchase request or status transition violates business rules.
 */
public class InvalidPurchaseException extends ApiException {

    public InvalidPurchaseException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}


