package com.bakery.api.promotion.dto;

import com.bakery.api.promotion.Promotion;
import com.bakery.api.promotion.dto.PromotionResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PromotionMapper {

    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "productName", source = "product.name")
    PromotionResponse toResponse(Promotion promotion);
}
