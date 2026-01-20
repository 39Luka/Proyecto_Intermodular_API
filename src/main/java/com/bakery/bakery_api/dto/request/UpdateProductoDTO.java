package com.bakery.bakery_api.dto.request;

public record UpdateProductoDTO(
        String nombre,
        String descripcion,
        Double precio,
        Integer stock
) {}


