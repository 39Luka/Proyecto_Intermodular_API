package com.bakery.api.promotion.exception;

import com.bakery.api.common.exception.ApiException;
import org.springframework.http.HttpStatus;

public class InvalidPromotionException extends ApiException {

    public InvalidPromotionException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}


