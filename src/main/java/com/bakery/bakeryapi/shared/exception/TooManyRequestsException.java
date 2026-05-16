package com.bakery.bakeryapi.shared.exception;

import org.springframework.http.HttpStatus;

/**
 * Se lanza cuando un cliente excede el límite de velocidad configurado.
 */
public class TooManyRequestsException extends ApiException {

    public TooManyRequestsException(String message) {
        super(message, HttpStatus.TOO_MANY_REQUESTS);
    }
}
