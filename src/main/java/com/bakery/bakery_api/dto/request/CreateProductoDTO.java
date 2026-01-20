package com.bakery.bakery_api.dto.request;

public record CreateProductoDTO(
        String nombre,
        String descripcion,
        Double precio,
        Integer stock
) {}

