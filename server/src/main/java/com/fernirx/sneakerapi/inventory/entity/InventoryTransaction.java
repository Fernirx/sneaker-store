package com.fernirx.sneakerapi.inventory.entity;

import com.fernirx.sneakerapi.common.entity.BaseCreatedEntity;
import com.fernirx.sneakerapi.inventory.enums.InventoryReferenceType;
import com.fernirx.sneakerapi.inventory.enums.InventoryTransactionType;
import com.fernirx.sneakerapi.product.entity.ProductVariant;
import com.fernirx.sneakerapi.user.entity.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Getter
@Setter
@Entity
@Table(name = "inventory_transactions", indexes = {
        @Index(name = "idx_inventory_variant_date",
                columnList = "variant_id, created_at"),
        @Index(name = "idx_inventory_created_by",
                columnList = "created_by"),
        @Index(name = "idx_inventory_type",
                columnList = "type"),
        @Index(name = "idx_inventory_reference",
                columnList = "reference_type, reference_id")})
public class InventoryTransaction extends BaseCreatedEntity {
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "variant_id", nullable = false)
    private ProductVariant variant;

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.SET_NULL)
    @JoinColumn(name = "created_by")
    private User createdBy;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private InventoryTransactionType type;

    @NotNull
    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @NotNull
    @Column(name = "old_stock", nullable = false)
    private Integer oldStock;

    @NotNull
    @Column(name = "new_stock", nullable = false)
    private Integer newStock;

    @Enumerated(EnumType.STRING)
    @Column(name = "reference_type")
    private InventoryReferenceType referenceType;

    @Column(name = "reference_id")
    private Long referenceId;

    @Column(name = "note", columnDefinition = "TEXT")
    private String note;
}