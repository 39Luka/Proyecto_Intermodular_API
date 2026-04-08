package com.bakery.bakeryapi.user.exception;

import com.bakery.bakeryapi.shared.exception.ApiException;
import org.springframework.http.HttpStatus;

public class EmailAlreadyExistsException extends ApiException {

    public EmailAlreadyExistsException(String email) {
        super("Email '" + email + "' already exists", HttpStatus.CONFLICT);
    }
}

