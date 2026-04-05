package com.bakery.bakeryapi.purchasess.dto;

import com.bakery.bakeryapi.purchasess.Purchase;
import com.bakery.bakeryapi.purchasess.dto.PurchaseResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = PurchaseItemMapper.class)
public interface PurchaseMapper {

    @Mapping(target = "userId", source = "user.id")
    PurchaseResponse toResponse(Purchase purchase);
}
