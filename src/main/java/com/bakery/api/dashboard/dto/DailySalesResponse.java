package com.bakery.api.dashboard.dto;

import java.time.LocalDate;
import java.util.List;

public record DailySalesResponse(
        LocalDate from,
        LocalDate to,
        List<DailySalesPointResponse> points
) {
}
