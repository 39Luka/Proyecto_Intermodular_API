package com.bakery.bakeryapi.product.dto;

import com.bakery.bakeryapi.domain.Product;
import com.bakery.bakeryapi.product.dto.ProductResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import java.util.Base64;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    @Mapping(target = "categoryId", source = "category.id")
    @Mapping(target = "imageBase64", expression = "java(productToImageBase64(product))")
    ProductResponse toResponse(Product product);

    default String productToImageBase64(Product product) {
        if (product.getImage() == null) {
            return null;
        }
        return Base64.getEncoder().encodeToString(product.getImage());
    }

    default byte[] imageBase64ToProduct(String imageBase64) {
        if (imageBase64 == null || imageBase64.isBlank()) {
            return null;
        }
        try {
            return Base64.getDecoder().decode(imageBase64);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid base64 image encoding", e);
        }
    }
}
