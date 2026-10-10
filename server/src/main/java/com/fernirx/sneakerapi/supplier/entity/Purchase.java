package com.fernirx.sneakerapi.supplier.entity;

import com.fernirx.sneakerapi.common.entity.BaseAuditEntity;
import com.fernirx.sneakerapi.supplier.enums.PurchasePaymentStatus;
import com.fernirx.sneakerapi.supplier.enums.PurchaseStatus;
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
@Table(name = "purchases", indexes = {
        @Index(name = "idx_purchases_supplier",
                columnList = "supplier_id"),
        @Index(name = "idx_purchases_payment_status",
                columnList = "payment_status"),
        @Index(name = "idx_purchases_status",
                columnList = "status")}, uniqueConstraints = {@UniqueConstraint(name = "purchase_code_UNIQUE",
        columnNames = {"purchase_code"})})
public class Purchase extends BaseAuditEntity {
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "supplier_id", nullable = false)
    private Supplier supplier;

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.SET_NULL)
    @JoinColumn(name = "created_by")
    private User createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.SET_NULL)
    @JoinColumn(name = "received_by")
    private User receivedBy;

    @Size(max = 50)
    @NotNull
    @Column(name = "purchase_code", nullable = false, length = 50)
    private String purchaseCode;

    @Size(max = 100)
    @Column(name = "supplier_invoice_no", length = 100)
    private String supplierInvoiceNo;

    @NotNull
    @ColumnDefault("0.00")
    @Column(name = "subtotal", nullable = false, precision = 15, scale = 2)
    private BigDecimal subtotal;

    @NotNull
    @ColumnDefault("0.00")
    @Column(name = "discount_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal discountAmount;

    @NotNull
    @ColumnDefault("0.00")
    @Column(name = "tax_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal taxAmount;

    @NotNull
    @ColumnDefault("0.00")
    @Column(name = "shipping_cost", nullable = false, precision = 15, scale = 2)
    private BigDecimal shippingCost;

    @NotNull
    @Column(name = "total_cost", nullable = false, precision = 15, scale = 2)
    private BigDecimal totalCost;

    @NotNull
    @ColumnDefault("'UNPAID'")
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false)
    private PurchasePaymentStatus paymentStatus;

    @NotNull
    @ColumnDefault("'DRAFT'")
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private PurchaseStatus status;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "confirmed_at")
    private LocalDateTime confirmedAt;

    @Column(name = "received_at")
    private LocalDateTime receivedAt;

    @OneToMany(mappedBy = "purchase")
    private Set<PurchaseItem> purchaseItems = new LinkedHashSet<>();
}