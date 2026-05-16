package com.bakery.bakeryapi.product.exception;

import com.bakery.bakeryapi.shared.exception.ApiException;
import org.springframework.http.HttpStatus;

/**
 * Se lanza cuando una operación requiere un producto activo pero el producto está deshabilitado.
 */
public class ProductInactiveException extends ApiException {

    public ProductInactiveException(Long id) {
        super("Product with id '" + id + "' is inactive", HttpStatus.CONFLICT);
    }
}
