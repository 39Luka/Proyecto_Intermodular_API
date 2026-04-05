package com.bakery.bakeryapi.auth.refresh;

import com.bakery.bakeryapi.common.exception.ApiException;
import org.springframework.http.HttpStatus;

public class InvalidRefreshTokenException extends ApiException {

    public InvalidRefreshTokenException() {
        super("Invalid refresh token", HttpStatus.UNAUTHORIZED);
    }
}

