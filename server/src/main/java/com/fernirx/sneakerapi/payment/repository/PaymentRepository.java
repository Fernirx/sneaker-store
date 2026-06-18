package com.fernirx.sneakerapi.payment.repository;

import com.fernirx.sneakerapi.payment.entity.Payment;
import com.fernirx.sneakerapi.payment.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface PaymentRepository extends JpaRepository<Payment, Long>, JpaSpecificationExecutor<Payment> {
    long countByOrder_IdAndStatus(Long orderId, PaymentStatus status);
}