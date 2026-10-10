package com.fernirx.sneakerapi.order.repository;

import com.fernirx.sneakerapi.order.dto.request.OrderFilterRequest;
import com.fernirx.sneakerapi.order.entity.Order;
import com.fernirx.sneakerapi.order.enums.OrderPaymentStatus;
import com.fernirx.sneakerapi.order.enums.OrderStatus;
import com.fernirx.sneakerapi.order.enums.PaymentMethod;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

public class OrderSpec {

    public static Specification<Order> build(OrderFilterRequest filter) {
        return Specification
                .where(hasKeyword(filter.search()))
                .and(hasStatus(filter.status()))
                .and(hasPaymentStatus(filter.paymentStatus()))
                .and(hasPaymentMethod(filter.paymentMethod()))
                .and(fromDate(filter.fromDate()))
                .and(toDate(filter.toDate()));
    }

    private static Specification<Order> hasKeyword(String keyword) {
        return (root, query, cb) -> {
            if (!StringUtils.hasText(keyword)) return null;
            String pattern = "%" + keyword.toLowerCase() + "%";
            return cb.like(cb.lower(root.get("code")), pattern);
        };
    }

    private static Specification<Order> hasStatus(OrderStatus status) {
        return (root, query, cb) -> status == null ? null : cb.equal(root.get("status"), status);
    }

    private static Specification<Order> hasPaymentStatus(OrderPaymentStatus paymentStatus) {
        return (root, query, cb) -> paymentStatus == null ? null : cb.equal(root.get("paymentStatus"), paymentStatus);
    }

    private static Specification<Order> hasPaymentMethod(PaymentMethod paymentMethod) {
        return (root, query, cb) -> paymentMethod == null ? null : cb.equal(root.get("paymentMethod"), paymentMethod);
    }

    private static Specification<Order> fromDate(LocalDateTime fromDate) {
        return (root, query, cb) -> fromDate == null ? null : cb.greaterThanOrEqualTo(root.get("createdAt"), fromDate);
    }

    private static Specification<Order> toDate(LocalDateTime toDate) {
        return (root, query, cb) -> toDate == null ? null : cb.lessThanOrEqualTo(root.get("createdAt"), toDate);
    }
}
