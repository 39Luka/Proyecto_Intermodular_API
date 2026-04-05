package com.bakery.bakeryapi.exception.user;

import com.bakery.bakeryapi.exception.ApiException;
import org.springframework.http.HttpStatus;

public class UserDisabledException extends ApiException {

    public UserDisabledException() {
        super("User is disabled", HttpStatus.FORBIDDEN);
    }
}


