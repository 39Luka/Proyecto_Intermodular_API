package com.bakery.bakeryapi.category.exception;

import com.bakery.bakeryapi.shared.exception.ApiException;
import org.springframework.http.HttpStatus;

/**
 * Se lanza cuando no se puede encontrar una categoría.
 */
public class CategoryNotFoundException extends ApiException {

    public CategoryNotFoundException(Long id) {
        super("Categoría con id '" + id + "' no encontrada", HttpStatus.NOT_FOUND);
    }
}


