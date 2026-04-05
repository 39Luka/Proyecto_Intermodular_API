package com.bakery.bakeryapi.catalog.category.dto;

import com.bakery.bakeryapi.catalog.category.Category;
import com.bakery.bakeryapi.catalog.category.dto.CategoryResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CategoryMapper {

    CategoryResponse toResponse(Category category);
}
