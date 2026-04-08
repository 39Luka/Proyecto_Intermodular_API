package com.bakery.bakeryapi.auth.exception;

import com.bakery.bakeryapi.shared.exception.ApiException;
import org.springframework.http.HttpStatus;

public class InvalidCredentialsException extends ApiException {

    public InvalidCredentialsException() {
        super("Invalid credentials", HttpStatus.UNAUTHORIZED);
    }
}


