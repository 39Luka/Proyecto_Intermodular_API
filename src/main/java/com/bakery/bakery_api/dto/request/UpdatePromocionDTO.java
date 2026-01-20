package com.bakery.bakery_api.dto.request;

import java.time.LocalDate;

public record UpdatePromocionDTO(
        String descripcion,
        Double descuento,
        LocalDate fechaInicio,
        LocalDate fechaFin
) {}
