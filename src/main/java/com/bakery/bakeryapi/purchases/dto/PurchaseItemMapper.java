package com.bakery.bakeryapi.purchasess.dto;

import com.bakery.bakeryapi.purchasess.PurchaseItem;
import com.bakery.bakeryapi.purchasess.dto.PurchaseItemResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PurchaseItemMapper {

    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "productName", source = "product.name")
    @Mapping(target = "promotionId", source = "promotion.id")
    PurchaseItemResponse toResponse(PurchaseItem item);
}
