package com.bakery.bakeryapi.dashboard.dto;

import java.math.BigDecimal;

public record DashboardSummaryResponse(
        long totalUsers,
        long totalProducts,
        long totalPurchases,
        long purchasesCreated,
        long purchasesPaid,
        long purchasesCancelled,
        BigDecimal revenuePaid
) {
}
