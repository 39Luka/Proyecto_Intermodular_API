package com.bakery.bakeryapi.product.exception;

import com.bakery.bakeryapi.shared.exception.ApiException;
import org.springframework.http.HttpStatus;

/**
 * Raised when an operation requires an active product but the product is disabled.
 */
public class ProductInactiveException extends ApiException {

    public ProductInactiveException(Long id) {
        super("Product with id '" + id + "' is inactive", HttpStatus.CONFLICT);
    }
}
