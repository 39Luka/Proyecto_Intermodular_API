package com.bakery.bakeryapi.dto.category;

import com.bakery.bakeryapi.domain.Category;
import com.bakery.bakeryapi.dto.category.CategoryResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CategoryMapper {

    CategoryResponse toResponse(Category category);
}
