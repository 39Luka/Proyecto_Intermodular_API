package com.bakery.bakeryapi.catalog.category.exception;

import com.bakery.bakeryapi.common.exception.ApiException;
import org.springframework.http.HttpStatus;

public class CategoryAlreadyExistsException extends ApiException {

    public CategoryAlreadyExistsException(String name) {
        super("Category '" + name + "' already exists", HttpStatus.CONFLICT);
    }
}


