package com.bakery.bakeryapi.userss.exception;

import com.bakery.bakeryapi.common.exception.ApiException;
import org.springframework.http.HttpStatus;

public class UserDisabledException extends ApiException {

    public UserDisabledException() {
        super("User is disabled", HttpStatus.FORBIDDEN);
    }
}


