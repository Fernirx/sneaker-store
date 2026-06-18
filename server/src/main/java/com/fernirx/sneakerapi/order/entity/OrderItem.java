package com.fernirx.sneakerapi.order.entity;

import com.fernirx.sneakerapi.common.entity.BaseEntity;
import com.fernirx.sneakerapi.product.entity.ProductVariant;
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
@Table(name = "order_items", indexes = {
        @Index(name = "idx_order_items_order",
                columnList = "order_id"),
        @Index(name = "idx_order_items_variant",
                columnList = "variant_id")})
public class OrderItem extends BaseEntity {
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "variant_id", nullable = false)
    private ProductVariant variant;

    @Size(max = 50)
    @NotNull
    @Column(name = "product_code", nullable = false, length = 50)
    private String productCode;

    @Size(max = 255)
    @NotNull
    @Column(name = "product_name", nullable = false)
    private String productName;

    @Size(max = 100)
    @NotNull
    @Column(name = "variant_sku", nullable = false, length = 100)
    private String variantSku;

    @NotNull
    @Column(name = "variant_size", columnDefinition = "tinyint UNSIGNED not null")
    private Short variantSize;

    @Size(max = 100)
    @NotNull
    @Column(name = "variant_color", nullable = false, length = 100)
    private String variantColor;

    @NotNull
    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "original_price", precision = 15, scale = 2)
    private BigDecimal originalPrice;

    @NotNull
    @Column(name = "unit_price", nullable = false, precision = 15, scale = 2)
    private BigDecimal unitPrice;

    @NotNull
    @Column(name = "subtotal", nullable = false, precision = 15, scale = 2)
    private BigDecimal subtotal;
}