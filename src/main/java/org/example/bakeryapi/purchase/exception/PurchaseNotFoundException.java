package org.example.bakeryapi.purchase.exception;

import org.example.bakeryapi.common.exception.ApiException;
import org.springframework.http.HttpStatus;

public class PurchaseNotFoundException extends ApiException {

    public PurchaseNotFoundException(Long id) {
        super("Purchase with id '" + id + "' not found", HttpStatus.NOT_FOUND);
    }
}


