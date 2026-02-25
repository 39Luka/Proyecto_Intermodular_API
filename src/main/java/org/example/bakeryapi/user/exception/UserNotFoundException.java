package org.example.bakeryapi.user.exception;

import org.example.bakeryapi.common.exception.ApiException;
import org.springframework.http.HttpStatus;

public class UserNotFoundException extends ApiException {

    public UserNotFoundException(Long id) {
        super("User with id " + id + " not found", HttpStatus.NOT_FOUND);
    }

    public UserNotFoundException(String email) {
        super("User with email " + email + " not found", HttpStatus.NOT_FOUND);
    }
}


