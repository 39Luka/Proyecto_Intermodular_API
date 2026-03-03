package com.bakery.api.product.exception;

import com.bakery.api.common.exception.ApiException;
import org.springframework.http.HttpStatus;

public class ProductInactiveException extends ApiException {

    public ProductInactiveException(Long id) {
        super("Product with id '" + id + "' is inactive", HttpStatus.CONFLICT);
    }
}
