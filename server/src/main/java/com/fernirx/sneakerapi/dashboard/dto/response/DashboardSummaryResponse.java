package com.fernirx.sneakerapi.dashboard.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public record DashboardSummaryResponse(
        BigDecimal revenueToday,
        BigDecimal revenueThisMonth,
        Map<String, Long> orderCountByStatus,
        List<DailyRevenue> revenueByDay,
        long lowStockCount,
        long outOfStockCount,
        List<LowStockVariant> lowStockVariants,
        List<TopProduct> topProducts,
        long newCustomersThisMonth,
        long pendingPurchases,
        long pendingStockAdjustments
) {
    public record DailyRevenue(LocalDate date, BigDecimal revenue) {}

    public record LowStockVariant(
            Long variantId, Long productId, String productName, String sku,
            String colorway, Short size, Integer stockQuantity, Integer minStockLevel
    ) {}

    public record TopProduct(String productCode, String productName, Long totalQuantity) {}
}
