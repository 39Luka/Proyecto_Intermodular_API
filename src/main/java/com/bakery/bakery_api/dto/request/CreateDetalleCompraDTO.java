package com.bakery.bakery_api.dto.request;

public record CreateDetalleCompraDTO(
        Long compraId,
        Long productoId,
        Integer cantidad,
        Double subtotal
) {}

