package com.bakery.bakery_api.dto.response;

import com.bakery.bakery_api.domain.Promocion;

import java.time.LocalDate;

public record PromocionDTO(
        Long id,
        String descripcion,
        Double descuento,
        LocalDate fechaInicio,
        LocalDate fechaFin
) {
    public static PromocionDTO fromEntity(Promocion promo) {
        return new PromocionDTO(
                promo.getId(),
                promo.getDescripcion(),
                promo.getDescuento(),
                promo.getFechaInicio(),
                promo.getFechaFin()
        );
    }
}
