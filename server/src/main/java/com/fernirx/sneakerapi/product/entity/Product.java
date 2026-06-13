package com.fernirx.sneakerapi.product.entity;

import com.fernirx.sneakerapi.brand.entity.Brand;
import com.fernirx.sneakerapi.product.enums.ClosureType;
import com.fernirx.sneakerapi.product.enums.Gender;
import com.fernirx.sneakerapi.product.enums.ShaftStyle;
import com.fernirx.sneakerapi.common.entity.BaseAuditEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.math.BigDecimal;

@Getter
@Setter
@Entity
@Table(name = "products", indexes = {
        @Index(name = "idx_products_brand_active",
                columnList = "brand_id, active"),
        @Index(name = "idx_products_style_code",
                columnList = "style_code"),
        @Index(name = "idx_products_gender",
                columnList = "gender"),
        @Index(name = "idx_products_badges",
                columnList = "new_arrival, on_sale")}, uniqueConstraints = {
        @UniqueConstraint(name = "code_UNIQUE",
                columnNames = {"code"}),
        @UniqueConstraint(name = "slug_UNIQUE",
                columnNames = {"slug"})})
public class Product extends BaseAuditEntity {
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "brand_id", nullable = false)
    private Brand brand;

    @Size(max = 50)
    @NotNull
    @Column(name = "code", nullable = false, length = 50)
    private String code;

    @Size(max = 50)
    @Column(name = "style_code", length = 50)
    private String styleCode;

    @Size(max = 255)
    @NotNull
    @Column(name = "name", nullable = false)
    private String name;

    @Size(max = 255)
    @NotNull
    @Column(name = "slug", nullable = false)
    private String slug;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @NotNull
    @ColumnDefault("'UNISEX'")
    @Enumerated(EnumType.STRING)
    @Column(name = "gender", nullable = false)
    private Gender gender;

    @Size(max = 100)
    @Column(name = "upper_material", length = 100)
    private String upperMaterial;

    @Size(max = 100)
    @Column(name = "sole_type", length = 100)
    private String soleType;

    @Enumerated(EnumType.STRING)
    @Column(name = "closure_type")
    private ClosureType closureType;

    @Enumerated(EnumType.STRING)
    @Column(name = "shaft_style")
    private ShaftStyle shaftStyle;

    @NotNull
    @Column(name = "base_price", nullable = false, precision = 15, scale = 2)
    private BigDecimal basePrice;

    @Column(name = "original_price", precision = 15, scale = 2)
    private BigDecimal originalPrice;

    @Column(name = "cost_price", precision = 15, scale = 2)
    private BigDecimal costPrice;

    @NotNull
    @ColumnDefault("0")
    @Column(name = "new_arrival", nullable = false)
    private Boolean newArrival;

    @NotNull
    @ColumnDefault("0")
    @Column(name = "on_sale", nullable = false)
    private Boolean onSale;

    @NotNull
    @ColumnDefault("1")
    @Column(name = "active", nullable = false)
    private Boolean active;

    @ColumnDefault("'0'")
    @Column(name = "sold_count", columnDefinition = "int UNSIGNED", nullable = false)
    private Integer soldCount;

    @ColumnDefault("'0'")
    @Column(name = "view_count", columnDefinition = "int UNSIGNED", nullable = false)
    private Integer viewCount;
}