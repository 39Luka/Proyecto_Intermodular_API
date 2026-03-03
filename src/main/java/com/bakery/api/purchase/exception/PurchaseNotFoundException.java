package com.bakery.api.purchase.exception;

import com.bakery.api.common.exception.ApiException;
import org.springframework.http.HttpStatus;

public class PurchaseNotFoundException extends ApiException {

    public PurchaseNotFoundException(Long id) {
        super("Purchase with id '" + id + "' not found", HttpStatus.NOT_FOUND);
    }
}


