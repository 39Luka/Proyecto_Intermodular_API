package com.bakery.bakery_api.dto.response;

import com.bakery.bakery_api.domain.Compra;
import java.time.LocalDate;

public record CompraDTO(
        Long id,
        Long usuarioId,
        LocalDate fecha,
        String estado,
        Long promocionId
) {
    public static CompraDTO fromEntity(Compra compra) {
        return new CompraDTO(
                compra.getId(),
                compra.getUsuario() != null ? compra.getUsuario().getId() : null,
                compra.getFecha(),
                compra.getEstado().name(),
                compra.getPromocion() != null ? compra.getPromocion().getId() : null
        );
    }
}
