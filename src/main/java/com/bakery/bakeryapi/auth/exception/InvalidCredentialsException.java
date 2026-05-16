package com.bakery.bakeryapi.auth.exception;

import com.bakery.bakeryapi.shared.exception.ApiException;
import org.springframework.http.HttpStatus;

/**
 * Se lanza cuando las credenciales de inicio de sesión o actualización de token son inválidas.
 */
public class InvalidCredentialsException extends ApiException {

    public InvalidCredentialsException() {
        super("Invalid credentials", HttpStatus.UNAUTHORIZED);
    }
}


