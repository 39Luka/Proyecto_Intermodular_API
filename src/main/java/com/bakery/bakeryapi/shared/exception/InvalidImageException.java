package com.bakery.bakeryapi.shared.exception;

import org.springframework.http.HttpStatus;

/**
 * Se lanza cuando una carga de imagen es inválida.
 */
public class InvalidImageException extends ApiException {
    public InvalidImageException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}
