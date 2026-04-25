package com.bakery.bakeryapi.category.dto;

public record CategoryResponse(
        Long id,
        String name
) {
    public static CategoryResponse from(com.bakery.bakeryapi.domain.Category category) {
        return new CategoryResponse(category.getId(), category.getName());
    }
}


