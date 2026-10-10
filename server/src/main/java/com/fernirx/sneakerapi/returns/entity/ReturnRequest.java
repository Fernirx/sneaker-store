package com.fernirx.sneakerapi.returns.entity;

import com.fernirx.sneakerapi.common.entity.BaseAuditEntity;
import com.fernirx.sneakerapi.customer.entity.Customer;
import com.fernirx.sneakerapi.order.entity.Order;
import com.fernirx.sneakerapi.returns.enums.ReturnResolutionType;
import com.fernirx.sneakerapi.returns.enums.ReturnStatus;
import com.fernirx.sneakerapi.user.entity.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "return_requests", indexes = {
        @Index(name = "idx_return_requests_order", columnList = "order_id"),
        @Index(name = "idx_return_requests_customer_status", columnList = "customer_id, status")},
        uniqueConstraints = {@UniqueConstraint(name = "code_UNIQUE", columnNames = {"code"})})
public class ReturnRequest extends BaseAuditEntity {
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Size(max = 50)
    @NotNull
    @Column(name = "code", nullable = false, length = 50)
    private String code;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "resolution_type", nullable = false)
    private ReturnResolutionType resolutionType;

    @NotNull
    @ColumnDefault("'PENDING'")
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ReturnStatus status;

    @NotNull
    @Column(name = "reason", nullable = false, columnDefinition = "TEXT")
    private String reason;

    @Column(name = "reject_reason", columnDefinition = "TEXT")
    private String rejectReason;

    @Size(max = 100)
    @Column(name = "tracking_code", length = 100)
    private String trackingCode;

    @Column(name = "refund_amount", precision = 15, scale = 2)
    private BigDecimal refundAmount;

    @Column(name = "refunded_at")
    private LocalDateTime refundedAt;

    @Size(max = 50)
    @Column(name = "exchange_shipping_order_code", length = 50)
    private String exchangeShippingOrderCode;

    @Column(name = "exchange_expected_delivery_at")
    private LocalDateTime exchangeExpectedDeliveryAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.SET_NULL)
    @JoinColumn(name = "approved_by")
    private User approvedBy;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "received_at")
    private LocalDateTime receivedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "admin_note", columnDefinition = "TEXT")
    private String adminNote;
}
