package com.bakery.bakeryapi.category.dto;

/**
 * Category data returned by the API.
 */
public record CategoryResponse(
        Long id,
        String name
) {
    public static CategoryResponse from(com.bakery.bakeryapi.domain.Category category) {
        return new CategoryResponse(category.getId(), category.getName());
    }
}


