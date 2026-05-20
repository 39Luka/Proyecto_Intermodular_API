package com.bakery.bakeryapi.user.exception;

import com.bakery.bakeryapi.shared.exception.ApiException;
import org.springframework.http.HttpStatus;

/**
 * Se lanza cuando un usuario deshabilitado intenta un flujo autenticado.
 */
public class UserDisabledException extends ApiException {

    public UserDisabledException() {
        super("El usuario está deshabilitado", HttpStatus.FORBIDDEN);
    }
}


