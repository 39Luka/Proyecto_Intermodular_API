package com.bakery.bakeryapi.purchase.exception;

import com.bakery.bakeryapi.shared.exception.ApiException;
import org.springframework.http.HttpStatus;

/**
 * Se lanza cuando una solicitud de compra o transición de estado viola reglas de negocio.
 */
public class InvalidPurchaseException extends ApiException {

    public InvalidPurchaseException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}


