package org.example.bakeryapi.promotion.exception;

import org.example.bakeryapi.common.exception.ApiException;
import org.springframework.http.HttpStatus;

public class PromotionNotFoundException extends ApiException {

    public PromotionNotFoundException(Long id) {
        super("Promotion with id '" + id + "' not found", HttpStatus.NOT_FOUND);
    }
}


