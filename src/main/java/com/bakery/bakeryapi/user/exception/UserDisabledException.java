package com.bakery.bakeryapi.user.exception;

import com.bakery.bakeryapi.shared.exception.ApiException;
import org.springframework.http.HttpStatus;

/**
 * Raised when a disabled user attempts an authenticated flow.
 */
public class UserDisabledException extends ApiException {

    public UserDisabledException() {
        super("User is disabled", HttpStatus.FORBIDDEN);
    }
}


