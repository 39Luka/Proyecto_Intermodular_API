package com.bakery.bakery_api.dto.request;

import java.time.LocalDate;
import com.bakery.bakery_api.domain.Compra;

public record CreateCompraDTO(
        Long usuarioId,
        LocalDate fecha,
        Compra.Estado estado,
        Long promocionId
) {}

