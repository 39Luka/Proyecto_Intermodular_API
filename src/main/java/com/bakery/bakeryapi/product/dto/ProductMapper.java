package com.bakery.bakeryapi.product.dto;

import com.bakery.bakeryapi.domain.Product;
import com.bakery.bakeryapi.product.dto.ProductResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    @Mapping(target = "categoryId", source = "category.id")
    ProductResponse toResponse(Product product);
}
