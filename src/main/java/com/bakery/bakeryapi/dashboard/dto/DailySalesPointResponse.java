package com.bakery.bakeryapi.dashboard.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record DailySalesPointResponse(
        LocalDate day,
        BigDecimal revenue,
        long purchases
) {
}
