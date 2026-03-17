package com.bakery.api.purchase.dto;

import com.bakery.api.purchase.domain.Purchase;
import com.bakery.api.purchase.dto.response.PurchaseResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = PurchaseItemMapper.class)
public interface PurchaseMapper {

    @Mapping(target = "userId", source = "user.id")
    PurchaseResponse toResponse(Purchase purchase);
}

