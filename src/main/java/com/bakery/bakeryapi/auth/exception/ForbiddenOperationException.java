package com.bakery.bakeryapi.auth.exception;

import com.bakery.bakeryapi.common.exception.ApiException;
import org.springframework.http.HttpStatus;

public class ForbiddenOperationException extends ApiException {

    public ForbiddenOperationException(String message) {
        super(message, HttpStatus.FORBIDDEN);
    }

    public ForbiddenOperationException() {
        super("Forbidden operation", HttpStatus.FORBIDDEN);
    }
}


