package com.bakery.api.user.exception;

import com.bakery.api.common.exception.ApiException;
import org.springframework.http.HttpStatus;

public class UserDisabledException extends ApiException {

    public UserDisabledException() {
        super("User is disabled", HttpStatus.FORBIDDEN);
    }
}


