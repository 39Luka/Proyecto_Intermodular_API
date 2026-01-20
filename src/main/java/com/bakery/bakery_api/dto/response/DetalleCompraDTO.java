package com.bakery.bakery_api.dto.response;

import com.bakery.bakery_api.domain.DetalleCompra;

public record DetalleCompraDTO(
        Long id,
        Long compraId,
        Long productoId,
        Integer cantidad,
        Double subtotal
) {
    public static DetalleCompraDTO fromEntity(DetalleCompra detalle) {
        return new DetalleCompraDTO(
                detalle.getId(),
                detalle.getCompra() != null ? detalle.getCompra().getId() : null,
                detalle.getProducto() != null ? detalle.getProducto().getId() : null,
                detalle.getCantidad(),
                detalle.getSubtotal()
        );
    }
}
