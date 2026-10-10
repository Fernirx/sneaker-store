package com.fernirx.sneakerapi.cart.entity;

import com.fernirx.sneakerapi.common.entity.BaseAuditEntity;
import com.fernirx.sneakerapi.product.entity.ProductVariant;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Getter
@Setter
@Entity
@Table(name = "cart_items", indexes = {@Index(name = "idx_cart_items_variant",
        columnList = "variant_id")}, uniqueConstraints = {@UniqueConstraint(name = "cart_variant_UNIQUE",
        columnNames = {
                "cart_id",
                "variant_id"})})
public class CartItem extends BaseAuditEntity {
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "cart_id", nullable = false)
    private Cart cart;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "variant_id", nullable = false)
    private ProductVariant variant;

    @NotNull
    @ColumnDefault("1")
    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @NotNull
    @ColumnDefault("1")
    @Column(name = "selected", nullable = false)
    private Boolean selected = true;
}