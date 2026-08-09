package com.fernirx.sneakerapi.dashboard.service.impl;

import com.fernirx.sneakerapi.customer.repository.CustomerRepository;
import com.fernirx.sneakerapi.dashboard.dto.response.DashboardSummaryResponse;
import com.fernirx.sneakerapi.dashboard.dto.response.DashboardSummaryResponse.DailyRevenue;
import com.fernirx.sneakerapi.dashboard.dto.response.DashboardSummaryResponse.LowStockVariant;
import com.fernirx.sneakerapi.dashboard.dto.response.DashboardSummaryResponse.TopProduct;
import com.fernirx.sneakerapi.dashboard.service.DashboardService;
import com.fernirx.sneakerapi.inventory.enums.StockAdjustmentStatus;
import com.fernirx.sneakerapi.inventory.repository.StockAdjustmentRepository;
import com.fernirx.sneakerapi.order.enums.OrderStatus;
import com.fernirx.sneakerapi.order.repository.OrderItemRepository;
import com.fernirx.sneakerapi.order.repository.OrderRepository;
import com.fernirx.sneakerapi.product.entity.ProductVariant;
import com.fernirx.sneakerapi.product.repository.ProductVariantRepository;
import com.fernirx.sneakerapi.supplier.enums.PurchaseStatus;
import com.fernirx.sneakerapi.supplier.repository.PurchaseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardServiceImpl implements DashboardService {
    private static final int REVENUE_CHART_DAYS = 14;
    private static final int TOP_PRODUCTS_LIMIT = 5;
    private static final int LOW_STOCK_LIMIT = 5;

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductVariantRepository productVariantRepository;
    private final CustomerRepository customerRepository;
    private final PurchaseRepository purchaseRepository;
    private final StockAdjustmentRepository stockAdjustmentRepository;

    /**
     * Lấy dữ liệu tổng hợp cho trang Dashboard (Thống kê doanh thu, đơn hàng, tồn kho, v.v.).
     * Các dữ liệu này được query trực tiếp từ database, có thể sẽ nặng nếu dữ liệu lớn.
     */
    @Override
    public DashboardSummaryResponse getSummary(boolean isAdmin) {
        LocalDateTime startOfToday = LocalDate.now().atStartOfDay();
        LocalDateTime startOfMonth = LocalDate.now().withDayOfMonth(1).atStartOfDay();

        Map<String, Long> orderCountByStatus = new LinkedHashMap<>();
        for (OrderStatus status : OrderStatus.values()) {
            orderCountByStatus.put(status.name(), 0L);
        }
        for (Object[] row : orderRepository.countGroupByStatus()) {
            orderCountByStatus.put(((OrderStatus) row[0]).name(), (Long) row[1]);
        }

        List<LowStockVariant> lowStockVariants = productVariantRepository
                .findLowStockVariants(PageRequest.of(0, LOW_STOCK_LIMIT)).stream()
                .map(this::toLowStockVariant)
                .toList();

        List<TopProduct> topProducts = orderItemRepository.findTopSellingProducts(PageRequest.of(0, TOP_PRODUCTS_LIMIT)).stream()
                .map(row -> new TopProduct((String) row[0], (String) row[1], (Long) row[2]))
                .toList();

        BigDecimal todayRevenue = null;
        BigDecimal monthRevenue = null;
        List<DailyRevenue> revenueByDay = null;

        if (isAdmin) {
            BigDecimal rawTodayRevenue = orderRepository.sumRevenueSince(startOfToday);
            todayRevenue = rawTodayRevenue != null ? rawTodayRevenue : BigDecimal.ZERO;

            BigDecimal rawMonthRevenue = orderRepository.sumRevenueSince(startOfMonth);
            monthRevenue = rawMonthRevenue != null ? rawMonthRevenue : BigDecimal.ZERO;

            revenueByDay = buildRevenueByDay();
        }

        return new DashboardSummaryResponse(
                todayRevenue,
                monthRevenue,
                orderCountByStatus,
                revenueByDay,
                productVariantRepository.countLowStock(),
                productVariantRepository.countOutOfStock(),
                lowStockVariants,
                topProducts,
                customerRepository.countByCreatedAtGreaterThanEqual(startOfMonth),
                purchaseRepository.countByStatusIn(List.of(PurchaseStatus.DRAFT, PurchaseStatus.CONFIRMED)),
                stockAdjustmentRepository.countByStatus(StockAdjustmentStatus.DRAFT)
        );
    }

    /**
     * Lấy doanh thu theo từng ngày trong 14 ngày gần nhất.
     * Điền đủ 14 ngày liên tiếp (kể cả ngày doanh thu = 0) để biểu đồ đường trên FE 
     * không bị lệch trục thời gian.
     */
    private List<DailyRevenue> buildRevenueByDay() {
        LocalDateTime chartFrom = LocalDate.now().minusDays(REVENUE_CHART_DAYS - 1L).atStartOfDay();
        Map<LocalDate, BigDecimal> revenueByDate = orderRepository.findDailyRevenueSince(chartFrom).stream()
                .collect(Collectors.toMap(row -> toLocalDate(row[0]), row -> row[1] != null ? new BigDecimal(row[1].toString()) : BigDecimal.ZERO));

        List<DailyRevenue> result = new ArrayList<>(REVENUE_CHART_DAYS);
        for (int i = REVENUE_CHART_DAYS - 1; i >= 0; i--) {
            LocalDate day = LocalDate.now().minusDays(i);
            result.add(new DailyRevenue(day, revenueByDate.getOrDefault(day, BigDecimal.ZERO)));
        }
        return result;
    }

    /**
     * Helper chuyển đổi kiểu dữ liệu Date (từ native query) về LocalDate.
     */
    private LocalDate toLocalDate(Object value) {
        if (value instanceof LocalDate d) return d;
        if (value instanceof java.sql.Date d) return d.toLocalDate();
        return LocalDate.parse(value.toString());
    }

    /**
     * Helper map dữ liệu ProductVariant sang DTO LowStockVariant cho Dashboard.
     */
    private LowStockVariant toLowStockVariant(ProductVariant v) {
        return new LowStockVariant(v.getId(), v.getProduct().getId(), v.getProduct().getName(), v.getSku(),
                v.getColorway(), v.getSize(), v.getStockQuantity(), v.getMinStockLevel());
    }
}
