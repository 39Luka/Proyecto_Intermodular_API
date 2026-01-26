package com.bakery.bakery_api.dto.request;

import java.time.LocalDate;
import java.util.List;

public record CreateCompraDTO(
        Long usuarioId,
        LocalDate fecha,
        String estado,
        Long promocionId,
        List<ProductoCantidad> productos
) {

    public record ProductoCantidad(
            Long productoId,
            Integer cantidad
    ) {}
}
