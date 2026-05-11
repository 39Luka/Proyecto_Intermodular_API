package com.bakery.bakeryapi.category.exception;

import com.bakery.bakeryapi.shared.exception.ApiException;
import org.springframework.http.HttpStatus;

/**
 * Raised when a category name is already in use.
 */
public class CategoryAlreadyExistsException extends ApiException {

    public CategoryAlreadyExistsException(String name) {
        super("Category '" + name + "' already exists", HttpStatus.CONFLICT);
    }
}


