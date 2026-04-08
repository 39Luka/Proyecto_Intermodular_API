package com.bakery.bakeryapi.promotion.exception;

import com.bakery.bakeryapi.shared.exception.ApiException;
import org.springframework.http.HttpStatus;

public class InvalidPromotionException extends ApiException {

    public InvalidPromotionException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}


