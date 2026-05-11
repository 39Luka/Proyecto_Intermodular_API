package com.bakery.bakeryapi.shared.exception;

import org.springframework.http.HttpStatus;

/**
 * Raised when a client exceeds the configured rate limit.
 */
public class TooManyRequestsException extends ApiException {

    public TooManyRequestsException(String message) {
        super(message, HttpStatus.TOO_MANY_REQUESTS);
    }
}
