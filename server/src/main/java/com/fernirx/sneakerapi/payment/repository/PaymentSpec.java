package com.fernirx.sneakerapi.payment.repository;

import com.fernirx.sneakerapi.payment.dto.request.PaymentFilterRequest;
import com.fernirx.sneakerapi.payment.entity.Payment;
import com.fernirx.sneakerapi.payment.enums.PaymentStatus;
import org.springframework.data.jpa.domain.Specification;

public class PaymentSpec {

    public static Specification<Payment> build(PaymentFilterRequest filter) {
        return Specification
                .where(hasOrderId(filter.orderId()))
                .and(hasStatus(filter.status()));
    }

    private static Specification<Payment> hasOrderId(Long orderId) {
        return (root, query, cb) -> orderId == null ? null : cb.equal(root.get("order").get("id"), orderId);
    }

    private static Specification<Payment> hasStatus(PaymentStatus status) {
        return (root, query, cb) -> status == null ? null : cb.equal(root.get("status"), status);
    }
}
