package com.bakery.bakery_api.dto.response;

import com.bakery.bakery_api.domain.Producto;

public record ProductoDTO(
        Long id,
        String nombre,
        String descripcion,
        Double precio,
        Integer stock
) {
    public static ProductoDTO fromEntity(Producto producto) {
        return new ProductoDTO(
                producto.getId(),
                producto.getNombre(),
                producto.getDescripcion(),
                producto.getPrecio(),
                producto.getStock()
        );
    }
}
