package org.example.bakeryapi.user.exception;

import org.example.bakeryapi.common.exception.ApiException;
import org.springframework.http.HttpStatus;

public class UserDisabledException extends ApiException {

    public UserDisabledException() {
        super("User is disabled", HttpStatus.FORBIDDEN);
    }
}


