package com.fernirx.sneakerapi.order.entity;

import com.fernirx.sneakerapi.common.entity.BaseAuditEntity;
import com.fernirx.sneakerapi.coupon.entity.CouponUsage;
import com.fernirx.sneakerapi.customer.entity.Customer;
import com.fernirx.sneakerapi.order.enums.OrderPaymentStatus;
import com.fernirx.sneakerapi.order.enums.OrderStatus;
import com.fernirx.sneakerapi.order.enums.PaymentMethod;
import com.fernirx.sneakerapi.payment.entity.Payment;
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
import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "orders", indexes = {
        @Index(name = "idx_orders_customer_status",
                columnList = "customer_id, status"),
        @Index(name = "idx_orders_guest",
                columnList = "guest_token"),
        @Index(name = "idx_orders_payment_status",
                columnList = "payment_status"),
        @Index(name = "idx_orders_assigned",
                columnList = "assigned_to"),
        @Index(name = "idx_orders_created",
                columnList = "created_at")}, uniqueConstraints = {
        @UniqueConstraint(name = "code_UNIQUE", columnNames = {"code"}),
        @UniqueConstraint(name = "uq_orders_idempotency_key", columnNames = {"idempotency_key"})})
public class Order extends BaseAuditEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @Size(max = 64)
    @Column(name = "guest_token", length = 64)
    private String guestToken;

    @Size(max = 50)
    @NotNull
    @Column(name = "code", nullable = false, length = 50)
    private String code;

    @Size(max = 36)
    @Column(name = "idempotency_key", length = 36)
    private String idempotencyKey;

    @Size(max = 36)
    @Column(name = "tracking_token", length = 36, unique = true)
    private String trackingToken;

    @NotNull
    @ColumnDefault("'PENDING'")
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private OrderStatus status;

    @NotNull
    @ColumnDefault("'UNPAID'")
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false)
    private OrderPaymentStatus paymentStatus;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false)
    private PaymentMethod paymentMethod;

    @Size(max = 200)
    @NotNull
    @Column(name = "recipient_name", nullable = false, length = 200)
    private String recipientName;

    @Size(max = 20)
    @NotNull
    @Column(name = "recipient_phone", nullable = false, length = 20)
    private String recipientPhone;

    @Size(max = 255)
    @NotNull
    @Column(name = "shipping_street", nullable = false)
    private String shippingStreet;

    @Size(max = 100)
    @NotNull
    @Column(name = "shipping_ward", nullable = false, length = 100)
    private String shippingWard;

    @Size(max = 100)
    @NotNull
    @Column(name = "shipping_district", nullable = false, length = 100)
    private String shippingDistrict;

    @Size(max = 100)
    @NotNull
    @Column(name = "shipping_province", nullable = false, length = 100)
    private String shippingProvince;

    @NotNull
    @Column(name = "subtotal", nullable = false, precision = 15, scale = 2)
    private BigDecimal subtotal;

    @NotNull
    @ColumnDefault("0.00")
    @Column(name = "shipping_fee", nullable = false, precision = 15, scale = 2)
    private BigDecimal shippingFee;

    @NotNull
    @ColumnDefault("0.00")
    @Column(name = "discount_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal discountAmount;

    @NotNull
    @ColumnDefault("0.00")
    @Column(name = "tier_discount_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal tierDiscountAmount;

    @NotNull
    @Column(name = "total_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal totalAmount;

    @Size(max = 50)
    @Column(name = "coupon_code", length = 50)
    private String couponCode;

    @Column(name = "note", columnDefinition = "TEXT")
    private String note;

    @Column(name = "admin_note", columnDefinition = "TEXT")
    private String adminNote;

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.SET_NULL)
    @JoinColumn(name = "assigned_to")
    private User assignedTo;

    @NotNull
    @Column(name = "expired_at", nullable = false)
    private LocalDateTime expiredAt;

    @OneToOne(mappedBy = "order")
    private CouponUsage couponUsage;

    @OneToMany(mappedBy = "order")
    private Set<OrderItem> orderItems = new LinkedHashSet<>();

    @OneToMany(mappedBy = "order")
    private Set<OrderStatusHistory> orderStatusHistories = new LinkedHashSet<>();

    @OneToMany(mappedBy = "order")
    private Set<Payment> payments = new LinkedHashSet<>();
}