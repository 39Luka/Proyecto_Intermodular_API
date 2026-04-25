package com.bakery.bakeryapi.shared.exception;

import org.springframework.http.HttpStatus;

public class InvalidImageException extends ApiException {
    public InvalidImageException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}
