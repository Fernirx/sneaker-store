package com.fernirx.sneakerapi.supplier.repository;

import com.fernirx.sneakerapi.supplier.dto.request.PurchaseFilterRequest;
import com.fernirx.sneakerapi.supplier.entity.Purchase;
import com.fernirx.sneakerapi.supplier.enums.PurchasePaymentStatus;
import com.fernirx.sneakerapi.supplier.enums.PurchaseStatus;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

public class PurchaseSpec {

    public static Specification<Purchase> build(PurchaseFilterRequest filter) {
        return Specification
                .where(hasKeyword(filter.search()))
                .and(hasSupplier(filter.supplierId()))
                .and(hasStatus(filter.status()))
                .and(hasPaymentStatus(filter.paymentStatus()))
                .and(fromDate(filter.fromDate()))
                .and(toDate(filter.toDate()));
    }

    private static Specification<Purchase> hasKeyword(String keyword) {
        return (root, query, cb) -> {
            if (!StringUtils.hasText(keyword)) return null;
            String pattern = "%" + keyword.toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("purchaseCode")), pattern),
                    cb.like(cb.lower(root.get("supplierInvoiceNo")), pattern)
            );
        };
    }

    private static Specification<Purchase> hasSupplier(Long supplierId) {
        return (root, query, cb) -> supplierId == null ? null : cb.equal(root.get("supplier").get("id"), supplierId);
    }

    private static Specification<Purchase> hasStatus(PurchaseStatus status) {
        return (root, query, cb) -> status == null ? null : cb.equal(root.get("status"), status);
    }

    private static Specification<Purchase> hasPaymentStatus(PurchasePaymentStatus paymentStatus) {
        return (root, query, cb) -> paymentStatus == null ? null : cb.equal(root.get("paymentStatus"), paymentStatus);
    }

    private static Specification<Purchase> fromDate(LocalDateTime fromDate) {
        return (root, query, cb) -> fromDate == null ? null : cb.greaterThanOrEqualTo(root.get("createdAt"), fromDate);
    }

    private static Specification<Purchase> toDate(LocalDateTime toDate) {
        return (root, query, cb) -> toDate == null ? null : cb.lessThanOrEqualTo(root.get("createdAt"), toDate);
    }
}
