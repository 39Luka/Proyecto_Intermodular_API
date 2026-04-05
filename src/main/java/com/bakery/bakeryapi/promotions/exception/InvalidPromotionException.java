package com.bakery.bakeryapi.promotionss.exception;

import com.bakery.bakeryapi.common.exception.ApiException;
import org.springframework.http.HttpStatus;

public class InvalidPromotionException extends ApiException {

    public InvalidPromotionException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}


