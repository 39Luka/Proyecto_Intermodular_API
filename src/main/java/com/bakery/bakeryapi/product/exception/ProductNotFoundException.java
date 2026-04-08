package com.bakery.bakeryapi.product.exception;

import com.bakery.bakeryapi.shared.exception.ApiException;
import org.springframework.http.HttpStatus;

public class ProductNotFoundException extends ApiException {

    public ProductNotFoundException(Long id) {
        super("Product with id '" + id + "' not found", HttpStatus.NOT_FOUND);
    }
}


