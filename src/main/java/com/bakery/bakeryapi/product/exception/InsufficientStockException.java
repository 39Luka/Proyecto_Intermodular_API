package com.bakery.bakeryapi.product.exception;

import com.bakery.bakeryapi.shared.exception.ApiException;
import org.springframework.http.HttpStatus;

/**
 * Se lanza cuando una compra solicita más unidades de las que el producto tiene en stock.
 */
public class InsufficientStockException extends ApiException {

    public InsufficientStockException(Long productId, int available, int requested) {
        super(
                "Stock insuficiente para el producto '" + productId + "'. Disponible: " + available + ", solicitado: " + requested,
                HttpStatus.CONFLICT
        );
    }
}


