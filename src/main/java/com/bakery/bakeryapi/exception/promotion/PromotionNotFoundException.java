package com.bakery.bakeryapi.exception.promotion;

import com.bakery.bakeryapi.exception.ApiException;
import org.springframework.http.HttpStatus;

public class PromotionNotFoundException extends ApiException {

    public PromotionNotFoundException(Long id) {
        super("Promotion with id '" + id + "' not found", HttpStatus.NOT_FOUND);
    }
}


