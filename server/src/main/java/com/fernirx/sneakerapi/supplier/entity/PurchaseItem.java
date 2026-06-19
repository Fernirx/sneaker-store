package com.fernirx.sneakerapi.supplier.entity;

import com.fernirx.sneakerapi.common.entity.BaseAuditEntity;
import com.fernirx.sneakerapi.product.entity.ProductVariant;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.math.BigDecimal;

@Getter
@Setter
@Entity
@Table(name = "purchase_items", indexes = {
        @Index(name = "idx_purchase_items_purchase",
                columnList = "purchase_id"),
        @Index(name = "idx_purchase_items_variant",
                columnList = "variant_id")})
public class PurchaseItem extends BaseAuditEntity {
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "purchase_id", nullable = false)
    private Purchase purchase;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "variant_id", nullable = false)
    private ProductVariant variant;

    @NotNull
    @Column(name = "quantity_ordered", columnDefinition = "int UNSIGNED not null")
    private Integer quantityOrdered;

    @NotNull
    @ColumnDefault("'0'")
    @Column(name = "quantity_received", columnDefinition = "int UNSIGNED not null")
    private Integer quantityReceived;

    @NotNull
    @Column(name = "unit_cost", nullable = false, precision = 15, scale = 2)
    private BigDecimal unitCost;

    @NotNull
    @Column(name = "line_total", nullable = false, precision = 15, scale = 2)
    private BigDecimal lineTotal;

    @NotNull
    @ColumnDefault("'0'")
    @Column(name = "defective_qty", columnDefinition = "int UNSIGNED not null")
    private Integer defectiveQty;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;
}