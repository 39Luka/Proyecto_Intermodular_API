package com.bakery.bakeryapi.user.exception;

import com.bakery.bakeryapi.shared.exception.ApiException;
import org.springframework.http.HttpStatus;

/**
 * Se lanza cuando un correo electrónico ya está asignado a otra cuenta.
 */
public class EmailAlreadyExistsException extends ApiException {

    public EmailAlreadyExistsException(String email) {
        super("El correo '" + email + "' ya existe", HttpStatus.CONFLICT);
    }
}

