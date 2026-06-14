package com.fernirx.sneakerapi.product.entity;

import com.fernirx.sneakerapi.category.entity.Category;
import com.fernirx.sneakerapi.common.entity.BaseCreatedEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Getter
@Setter
@Entity
@Table(name = "product_categories", indexes = {@Index(name = "idx_product_categories_category",
        columnList = "category_id")}, uniqueConstraints = {@UniqueConstraint(name = "product_category_UNIQUE",
        columnNames = {
                "product_id",
                "category_id"})})
public class ProductCategory extends BaseCreatedEntity {
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;
}