package org.example.bakeryapi.category.exception;

import org.example.bakeryapi.common.exception.ApiException;
import org.springframework.http.HttpStatus;

public class CategoryNotFoundException extends ApiException {

    public CategoryNotFoundException(Long id) {
        super("Category with id '" + id + "' not found", HttpStatus.NOT_FOUND);
    }
}


