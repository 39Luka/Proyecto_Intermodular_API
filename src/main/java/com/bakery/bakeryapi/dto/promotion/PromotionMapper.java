package com.bakery.bakeryapi.dto.promotion;

import com.bakery.bakeryapi.domain.Promotion;
import com.bakery.bakeryapi.dto.promotion.PromotionResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PromotionMapper {

    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "productName", source = "product.name")
    PromotionResponse toResponse(Promotion promotion);
}
