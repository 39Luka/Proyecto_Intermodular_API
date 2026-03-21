package com.bakery.api.purchase.dto;

import com.bakery.api.purchase.PurchaseItem;
import com.bakery.api.purchase.dto.PurchaseItemResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PurchaseItemMapper {

    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "productName", source = "product.name")
    @Mapping(target = "promotionId", source = "promotion.id")
    PurchaseItemResponse toResponse(PurchaseItem item);
}
