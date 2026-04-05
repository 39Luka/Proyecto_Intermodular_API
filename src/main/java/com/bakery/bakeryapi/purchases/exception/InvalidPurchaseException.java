package com.bakery.bakeryapi.purchasess.exception;

import com.bakery.bakeryapi.common.exception.ApiException;
import org.springframework.http.HttpStatus;

public class InvalidPurchaseException extends ApiException {

    public InvalidPurchaseException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}


