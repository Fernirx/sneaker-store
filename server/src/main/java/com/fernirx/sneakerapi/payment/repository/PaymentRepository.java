package com.fernirx.sneakerapi.payment.repository;

import com.fernirx.sneakerapi.payment.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
}