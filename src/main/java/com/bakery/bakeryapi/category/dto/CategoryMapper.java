package com.bakery.bakeryapi.category.dto;

import com.bakery.bakeryapi.domain.Category;
import com.bakery.bakeryapi.category.dto.CategoryResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CategoryMapper {

    CategoryResponse toResponse(Category category);
}
