package com.bakery.bakeryapi.shared.exception;

import com.bakery.bakeryapi.shared.exception.ApiException;
import org.springframework.http.HttpStatus;

/**
 * Se lanza cuando el usuario autenticado no puede realizar una operación.
 */
public class ForbiddenOperationException extends ApiException {

    public ForbiddenOperationException(String message) {
        super(message, HttpStatus.FORBIDDEN);
    }

    public ForbiddenOperationException() {
        super("Forbidden operation", HttpStatus.FORBIDDEN);
    }
}


