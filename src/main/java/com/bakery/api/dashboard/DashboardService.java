package com.bakery.api.dashboard;

import com.bakery.api.dashboard.dto.DailySalesPointResponse;
import com.bakery.api.dashboard.dto.DailySalesResponse;
import com.bakery.api.dashboard.dto.DashboardSummaryResponse;
import com.bakery.api.purchase.PurchaseRepository;
import com.bakery.api.purchase.PurchaseStatus;
import com.bakery.api.product.ProductRepository;
import com.bakery.api.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional(readOnly = true)
public class DashboardService {

    private final PurchaseRepository purchaseRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final Clock clock;

    public DashboardService(
            PurchaseRepository purchaseRepository,
            UserRepository userRepository,
            ProductRepository productRepository,
            Clock clock
    ) {
        this.purchaseRepository = purchaseRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.clock = clock;
    }

    public DashboardSummaryResponse summary() {
        long totalUsers = userRepository.count();
        long totalProducts = productRepository.count();
        long totalPurchases = purchaseRepository.count();

        long created = purchaseRepository.countByStatus(PurchaseStatus.CREATED);
        long paid = purchaseRepository.countByStatus(PurchaseStatus.PAID);
        long cancelled = purchaseRepository.countByStatus(PurchaseStatus.CANCELLED);

        BigDecimal revenuePaid = purchaseRepository.sumTotalByStatus(PurchaseStatus.PAID);

        return new DashboardSummaryResponse(
                totalUsers,
                totalProducts,
                totalPurchases,
                created,
                paid,
                cancelled,
                revenuePaid
        );
    }

    public DailySalesResponse dailySales(int days) {
        int safeDays = days <= 0 ? 30 : Math.min(days, 365);

        LocalDate today = LocalDate.now(clock);
        LocalDate fromDate = today.minusDays(safeDays - 1L);

        // Use UTC so values are stable across deployments; UI can display in local timezone if needed.
        LocalDateTime from = fromDate.atStartOfDay();
        LocalDateTime toExclusive = today.plusDays(1).atStartOfDay();

        List<PurchaseRepository.PurchaseTotalRow> rows = purchaseRepository.findTotalsByStatusBetween(
                PurchaseStatus.PAID,
                from,
                toExclusive
        );

        Map<LocalDate, BigDecimal> revenueByDay = new HashMap<>();
        Map<LocalDate, Long> countByDay = new HashMap<>();

        for (PurchaseRepository.PurchaseTotalRow row : rows) {
            if (row == null || row.getCreatedAt() == null || row.getTotal() == null) {
                continue;
            }
            // LocalDateTime is stored without timezone (MySQL DATETIME), so group by its date component.
            LocalDate day = row.getCreatedAt().toLocalDate();
            revenueByDay.merge(day, row.getTotal(), BigDecimal::add);
            countByDay.merge(day, 1L, Long::sum);
        }

        List<DailySalesPointResponse> points = new ArrayList<>(safeDays);
        for (int i = 0; i < safeDays; i++) {
            LocalDate day = fromDate.plusDays(i);
            BigDecimal revenue = revenueByDay.getOrDefault(day, BigDecimal.ZERO);
            long count = countByDay.getOrDefault(day, 0L);
            points.add(new DailySalesPointResponse(day, revenue, count));
        }

        return new DailySalesResponse(fromDate, today, points);
    }
}
