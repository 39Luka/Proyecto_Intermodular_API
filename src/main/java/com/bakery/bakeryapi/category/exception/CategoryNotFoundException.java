package com.bakery.bakeryapi.category.exception;

import com.bakery.bakeryapi.shared.exception.ApiException;
import org.springframework.http.HttpStatus;

public class CategoryNotFoundException extends ApiException {

    public CategoryNotFoundException(Long id) {
        super("Category with id '" + id + "' not found", HttpStatus.NOT_FOUND);
    }
}


