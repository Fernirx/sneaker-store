package com.fernirx.sneakerapi.product.entity;

import com.fernirx.sneakerapi.common.entity.BaseCreatedEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Getter
@Setter
@Entity
@Table(name = "product_images", indexes = {
        @Index(name = "idx_images_primary",
                columnList = "product_id, colorway, primary_image"),
        @Index(name = "idx_images_product_colorway",
                columnList = "product_id, colorway")}, uniqueConstraints = {@UniqueConstraint(name = "image_public_id_UNIQUE",
        columnNames = {"image_public_id"})})
public class ProductImage extends BaseCreatedEntity {
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Size(max = 100)
    @NotNull
    @Column(name = "colorway", nullable = false, length = 100)
    private String colorway;

    @Size(max = 7)
    @Column(name = "color_hex", length = 7)
    private String colorHex;

    @Size(max = 255)
    @NotNull
    @Column(name = "image_public_id", nullable = false)
    private String imagePublicId;

    @NotNull
    @ColumnDefault("0")
    @Column(name = "primary_image", nullable = false)
    private Boolean primaryImage;

    @NotNull
    @ColumnDefault("0")
    @Column(name = "display_order", nullable = false)
    private Integer displayOrder;
}