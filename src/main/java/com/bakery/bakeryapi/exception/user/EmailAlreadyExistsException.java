package com.bakery.bakeryapi.exception.user;

import com.bakery.bakeryapi.exception.ApiException;
import org.springframework.http.HttpStatus;

public class EmailAlreadyExistsException extends ApiException {

    public EmailAlreadyExistsException(String email) {
        super("Email '" + email + "' already exists", HttpStatus.CONFLICT);
    }
}

