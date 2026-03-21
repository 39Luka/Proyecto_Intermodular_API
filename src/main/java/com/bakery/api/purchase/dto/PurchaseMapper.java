package com.bakery.api.purchase.dto;

import com.bakery.api.purchase.Purchase;
import com.bakery.api.purchase.dto.PurchaseResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = PurchaseItemMapper.class)
public interface PurchaseMapper {

    @Mapping(target = "userId", source = "user.id")
    PurchaseResponse toResponse(Purchase purchase);
}
