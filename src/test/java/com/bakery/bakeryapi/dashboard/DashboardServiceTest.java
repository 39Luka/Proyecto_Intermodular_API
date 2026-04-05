package com.bakery.bakeryapi.dashboard;

import com.bakery.bakeryapi.dashboard.dto.DailySalesResponse;
import com.bakery.bakeryapi.purchasess.PurchaseRepository;
import com.bakery.bakeryapi.purchasess.PurchaseStatus;
import com.bakery.bakeryapi.catalog.product.ProductRepository;
import com.bakery.bakeryapi.userss.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock
    private PurchaseRepository purchaseRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProductRepository productRepository;

    @Test
    void summary_returnsCountsAndRevenue() {
        Clock clock = Clock.fixed(Instant.parse("2026-03-21T10:00:00Z"), ZoneOffset.UTC);
        DashboardService service = new DashboardService(purchaseRepository, userRepository, productRepository, clock);

        when(userRepository.count()).thenReturn(5L);
        when(productRepository.count()).thenReturn(12L);
        when(purchaseRepository.count()).thenReturn(20L);
        when(purchaseRepository.countByStatus(PurchaseStatus.CREATED)).thenReturn(3L);
        when(purchaseRepository.countByStatus(PurchaseStatus.PAID)).thenReturn(15L);
        when(purchaseRepository.countByStatus(PurchaseStatus.CANCELLED)).thenReturn(2L);
        when(purchaseRepository.sumTotalByStatus(PurchaseStatus.PAID)).thenReturn(new BigDecimal("123.45"));

        var summary = service.summary();

        assertEquals(5L, summary.totalUsers());
        assertEquals(12L, summary.totalProducts());
        assertEquals(20L, summary.totalPurchases());
        assertEquals(3L, summary.purchasesCreated());
        assertEquals(15L, summary.purchasesPaid());
        assertEquals(2L, summary.purchasesCancelled());
        assertEquals(new BigDecimal("123.45"), summary.revenuePaid());
    }

    @Test
    void dailySales_aggregatesAndFillsMissingDays() {
        Clock clock = Clock.fixed(Instant.parse("2026-03-21T10:00:00Z"), ZoneOffset.UTC);
        DashboardService service = new DashboardService(purchaseRepository, userRepository, productRepository, clock);

        // Last 3 days inclusive: 2026-03-19, 2026-03-20, 2026-03-21
        List<PurchaseRepository.PurchaseTotalRow> rows = List.of(
                new Row(LocalDateTime.parse("2026-03-19T12:00:00"), new BigDecimal("10.00")),
                new Row(LocalDateTime.parse("2026-03-21T09:00:00"), new BigDecimal("7.50")),
                new Row(LocalDateTime.parse("2026-03-21T10:00:00"), new BigDecimal("2.50"))
        );
        when(purchaseRepository.findTotalsByStatusBetween(
                org.mockito.ArgumentMatchers.eq(PurchaseStatus.PAID),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any()
        )).thenReturn(rows);

        DailySalesResponse response = service.dailySales(3);

        assertEquals(3, response.points().size());

        assertEquals("2026-03-19", response.points().get(0).day().toString());
        assertEquals(new BigDecimal("10.00"), response.points().get(0).revenue());
        assertEquals(1L, response.points().get(0).purchases());

        assertEquals("2026-03-20", response.points().get(1).day().toString());
        assertEquals(new BigDecimal("0"), response.points().get(1).revenue());
        assertEquals(0L, response.points().get(1).purchases());

        assertEquals("2026-03-21", response.points().get(2).day().toString());
        assertEquals(new BigDecimal("10.00"), response.points().get(2).revenue());
        assertEquals(2L, response.points().get(2).purchases());
    }

    private record Row(LocalDateTime createdAt, BigDecimal total) implements PurchaseRepository.PurchaseTotalRow {
        @Override
        public LocalDateTime getCreatedAt() {
            return createdAt;
        }

        @Override
        public BigDecimal getTotal() {
            return total;
        }
    }
}
