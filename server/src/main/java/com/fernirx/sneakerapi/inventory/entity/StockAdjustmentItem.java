package com.fernirx.sneakerapi.inventory.entity;

import com.fernirx.sneakerapi.common.entity.BaseCreatedEntity;
import com.fernirx.sneakerapi.product.entity.ProductVariant;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Getter
@Setter
@Entity
@Table(name = "stock_adjustment_items", indexes = {
        @Index(name = "idx_adj_items_adjustment",
                columnList = "adjustment_id"),
        @Index(name = "idx_adj_items_variant",
                columnList = "variant_id")})
public class StockAdjustmentItem extends BaseCreatedEntity {
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "adjustment_id", nullable = false)
    private StockAdjustment adjustment;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "variant_id", nullable = false)
    private ProductVariant variant;

    @NotNull
    @Column(name = "quantity_change", nullable = false)
    private Integer quantityChange;

    @NotNull
    @Column(name = "quantity_before", nullable = false)
    private Integer quantityBefore;

    @NotNull
    @Column(name = "quantity_after", nullable = false)
    private Integer quantityAfter;

    @Size(max = 255)
    @Column(name = "note")
    private String note;
}
