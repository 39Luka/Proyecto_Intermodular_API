package com.bakery.bakeryapi.dto.product;

import com.bakery.bakeryapi.domain.Product;
import com.bakery.bakeryapi.dto.product.ProductResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    @Mapping(target = "categoryId", source = "category.id")
    ProductResponse toResponse(Product product);
}
