package com.bakery.bakeryapi.promotion.exception;

import com.bakery.bakeryapi.shared.exception.ApiException;
import org.springframework.http.HttpStatus;

/**
 * Se lanza cuando no se puede encontrar una promoción.
 */
public class PromotionNotFoundException extends ApiException {

    public PromotionNotFoundException(Long id) {
        super("Promoción con id '" + id + "' no encontrada", HttpStatus.NOT_FOUND);
    }
}


