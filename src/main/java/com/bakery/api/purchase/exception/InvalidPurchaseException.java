package com.bakery.api.purchase.exception;

import com.bakery.api.common.exception.ApiException;
import org.springframework.http.HttpStatus;

public class InvalidPurchaseException extends ApiException {

    public InvalidPurchaseException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}


