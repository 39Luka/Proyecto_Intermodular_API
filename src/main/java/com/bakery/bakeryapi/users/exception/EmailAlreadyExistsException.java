package com.bakery.bakeryapi.userss.exception;

import com.bakery.bakeryapi.common.exception.ApiException;
import org.springframework.http.HttpStatus;

public class EmailAlreadyExistsException extends ApiException {

    public EmailAlreadyExistsException(String email) {
        super("Email '" + email + "' already exists", HttpStatus.CONFLICT);
    }
}

