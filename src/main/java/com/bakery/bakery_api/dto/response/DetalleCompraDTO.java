package com.bakery.bakery_api.dto.response;

import com.bakery.bakery_api.domain.DetalleCompra;

public record DetalleCompraDTO(
        Long productoId,
        String nombreProducto,
        Integer cantidad,
        Double subtotal
) {
    public static DetalleCompraDTO fromEntity(DetalleCompra detalle) {
        return new DetalleCompraDTO(
                detalle.getProducto().getId(),
                detalle.getProducto().getNombre(),
                detalle.getCantidad(),
                detalle.getSubtotal()
        );
    }
}
