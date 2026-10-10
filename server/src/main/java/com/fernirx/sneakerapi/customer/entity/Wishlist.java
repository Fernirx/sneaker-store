package com.fernirx.sneakerapi.customer.entity;

import com.fernirx.sneakerapi.common.entity.BaseCreatedEntity;
import com.fernirx.sneakerapi.product.entity.Product;
import com.fernirx.sneakerapi.product.entity.ProductVariant;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Getter
@Setter
@Entity
@Table(name = "wishlists", indexes = {
        @Index(name = "idx_wishlists_customer",
                columnList = "customer_id"),
        @Index(name = "idx_wishlists_variant",
                columnList = "variant_id")}, uniqueConstraints = {@UniqueConstraint(name = "customer_product_UNIQUE",
        columnNames = {
                "customer_id",
                "product_id",
                "variant_id"})})
public class Wishlist extends BaseCreatedEntity {
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.SET_NULL)
    @JoinColumn(name = "variant_id")
    private ProductVariant variant;
}