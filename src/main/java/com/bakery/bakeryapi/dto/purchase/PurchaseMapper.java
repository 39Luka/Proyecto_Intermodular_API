package com.bakery.bakeryapi.dto.purchase;

import com.bakery.bakeryapi.domain.Purchase;
import com.bakery.bakeryapi.dto.purchase.PurchaseResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = PurchaseItemMapper.class)
public interface PurchaseMapper {

    @Mapping(target = "userId", source = "user.id")
    PurchaseResponse toResponse(Purchase purchase);
}
