package com.bakery.bakeryapi.shared.exception;

import com.bakery.bakeryapi.shared.exception.ApiException;
import org.springframework.http.HttpStatus;

/**
 * Raised when the authenticated user is not allowed to perform an operation.
 */
public class ForbiddenOperationException extends ApiException {

    public ForbiddenOperationException(String message) {
        super(message, HttpStatus.FORBIDDEN);
    }

    public ForbiddenOperationException() {
        super("Forbidden operation", HttpStatus.FORBIDDEN);
    }
}


