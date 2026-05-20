package com.bakery.bakeryapi.purchase.exception;

import com.bakery.bakeryapi.shared.exception.ApiException;
import org.springframework.http.HttpStatus;

/**
 * Se lanza cuando no se puede encontrar una compra.
 */
public class PurchaseNotFoundException extends ApiException {

    public PurchaseNotFoundException(Long id) {
        super("Compra con id '" + id + "' no encontrada", HttpStatus.NOT_FOUND);
    }
}


