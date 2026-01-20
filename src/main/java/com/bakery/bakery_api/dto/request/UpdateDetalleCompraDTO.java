package com.bakery.bakery_api.dto.request;

public record UpdateDetalleCompraDTO(
        Long compraId,
        Long productoId,
        Integer cantidad,
        Double subtotal
) {}
