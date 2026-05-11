package com.bakery.bakeryapi.promotion.exception;

import com.bakery.bakeryapi.shared.exception.ApiException;
import org.springframework.http.HttpStatus;

/**
 * Raised when a promotion is not applicable or violates promotion rules.
 */
public class InvalidPromotionException extends ApiException {

    public InvalidPromotionException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}


