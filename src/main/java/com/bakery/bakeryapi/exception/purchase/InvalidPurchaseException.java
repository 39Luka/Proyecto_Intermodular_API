package com.bakery.bakeryapi.exception.purchase;

import com.bakery.bakeryapi.exception.ApiException;
import org.springframework.http.HttpStatus;

public class InvalidPurchaseException extends ApiException {

    public InvalidPurchaseException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}


