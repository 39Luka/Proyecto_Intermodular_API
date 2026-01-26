package com.bakery.bakery_api.dto.response;

import com.bakery.bakery_api.domain.Compra;

import java.time.LocalDate;
import java.util.List;

public record CompraDTO(
        Long id,
        Long usuarioId,
        LocalDate fecha,
        String estado,
        Long promocionId,
        List<DetalleCompraDTO> detalles
) {

    public static CompraDTO fromEntity(Compra compra) {
        List<DetalleCompraDTO> detalles = compra.getDetalles() != null
                ? compra.getDetalles().stream()
                .map(DetalleCompraDTO::fromEntity)
                .toList()
                : List.of();

        return new CompraDTO(
                compra.getId(),
                compra.getUsuario() != null ? compra.getUsuario().getId() : null,
                compra.getFecha(),
                compra.getEstado().name(),
                compra.getPromocion() != null ? compra.getPromocion().getId() : null,
                detalles
        );
    }
}
