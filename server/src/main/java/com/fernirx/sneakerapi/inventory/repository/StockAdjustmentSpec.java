package com.fernirx.sneakerapi.inventory.repository;

import com.fernirx.sneakerapi.inventory.dto.request.StockAdjustmentFilterRequest;
import com.fernirx.sneakerapi.inventory.entity.StockAdjustment;
import com.fernirx.sneakerapi.inventory.enums.StockAdjustmentStatus;
import com.fernirx.sneakerapi.inventory.enums.StockAdjustmentType;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

public class StockAdjustmentSpec {

    public static Specification<StockAdjustment> build(StockAdjustmentFilterRequest filter) {
        return Specification
                .where(hasKeyword(filter.search()))
                .and(hasType(filter.type()))
                .and(hasStatus(filter.status()))
                .and(fromDate(filter.fromDate()))
                .and(toDate(filter.toDate()));
    }

    private static Specification<StockAdjustment> hasKeyword(String keyword) {
        return (root, query, cb) -> {
            if (!StringUtils.hasText(keyword)) return null;
            String pattern = "%" + keyword.toLowerCase() + "%";
            return cb.like(cb.lower(root.get("code")), pattern);
        };
    }

    private static Specification<StockAdjustment> hasType(StockAdjustmentType type) {
        return (root, query, cb) -> type == null ? null : cb.equal(root.get("type"), type);
    }

    private static Specification<StockAdjustment> hasStatus(StockAdjustmentStatus status) {
        return (root, query, cb) -> status == null ? null : cb.equal(root.get("status"), status);
    }

    private static Specification<StockAdjustment> fromDate(LocalDateTime fromDate) {
        return (root, query, cb) -> fromDate == null ? null : cb.greaterThanOrEqualTo(root.get("createdAt"), fromDate);
    }

    private static Specification<StockAdjustment> toDate(LocalDateTime toDate) {
        return (root, query, cb) -> toDate == null ? null : cb.lessThanOrEqualTo(root.get("createdAt"), toDate);
    }
}
