package com.bakery.bakery_api.dto.request;

import com.bakery.bakery_api.domain.Compra;

import java.time.LocalDate;

public record UpdateCompraDTO(
        Long usuarioId,
        LocalDate fecha,
        Compra.Estado estado,
        Long promocionId
) {}
