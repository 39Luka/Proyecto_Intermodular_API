package com.bakery.api.category.dto;

import com.bakery.api.category.Category;
import com.bakery.api.category.dto.response.CategoryResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CategoryMapper {

    CategoryResponse toResponse(Category category);
}

