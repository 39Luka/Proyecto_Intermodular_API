package com.bakery.bakeryapi.purchase.exception;

import com.bakery.bakeryapi.shared.exception.ApiException;
import org.springframework.http.HttpStatus;

public class InvalidPurchaseException extends ApiException {

    public InvalidPurchaseException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}


