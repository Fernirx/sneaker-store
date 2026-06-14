package com.fernirx.sneakerapi.product.entity;

import com.fernirx.sneakerapi.collection.entity.Collection;
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
@Table(name = "product_collections", indexes = {@Index(name = "idx_product_collections_collection",
        columnList = "collection_id")}, uniqueConstraints = {@UniqueConstraint(name = "product_collection_UNIQUE",
        columnNames = {
                "product_id",
                "collection_id"})})
public class ProductCollection extends BaseCreatedEntity {
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "collection_id", nullable = false)
    private Collection collection;
}