package com.fernirx.sneakerapi.payment.entity;

import com.fernirx.sneakerapi.common.entity.BaseCreatedEntity;
import com.fernirx.sneakerapi.order.entity.Order;
import com.fernirx.sneakerapi.payment.enums.PaymentStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.math.BigDecimal;

@Getter
@Setter
@Entity
@Table(name = "payments", indexes = {
        @Index(name = "idx_payments_order",
                columnList = "order_id"),
        @Index(name = "idx_payments_status",
                columnList = "status"),
        @Index(name = "idx_payments_transaction",
                columnList = "transaction_id")})
public class Payment extends BaseCreatedEntity {
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @NotNull
    @Column(name = "amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private PaymentStatus status;

    @Size(max = 255)
    @Column(name = "transaction_id")
    private String transactionId;

    @Size(max = 10)
    @Column(name = "response_code", length = 10)
    private String responseCode;
}