package com.bakery.bakeryapi.exception.promotion;

import com.bakery.bakeryapi.exception.ApiException;
import org.springframework.http.HttpStatus;

public class InvalidPromotionException extends ApiException {

    public InvalidPromotionException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}


