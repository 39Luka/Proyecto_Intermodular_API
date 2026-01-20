package com.bakery.bakery_api.dto.request;

import java.time.LocalDate;

public record CreatePromocionDTO(
        String descripcion,
        Double descuento,
        LocalDate fechaInicio,
        LocalDate fechaFin
) {}

