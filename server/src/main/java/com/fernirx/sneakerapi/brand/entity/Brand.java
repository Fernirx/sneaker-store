package com.fernirx.sneakerapi.brand.entity;

import com.fernirx.sneakerapi.common.entity.BaseAuditEntity;
import com.fernirx.sneakerapi.product.entity.Product;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "brands", indexes = {@Index(name = "idx_brands_active",
        columnList = "active")}, uniqueConstraints = {
        @UniqueConstraint(name = "name_UNIQUE",
                columnNames = {"name"}),
        @UniqueConstraint(name = "slug_UNIQUE",
                columnNames = {"slug"})})
public class Brand extends BaseAuditEntity {
    @Size(max = 100)
    @NotNull
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Size(max = 100)
    @NotNull
    @Column(name = "slug", nullable = false, length = 100)
    private String slug;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Size(max = 255)
    @Column(name = "logo_public_id")
    private String logoPublicId;

    @NotNull
    @ColumnDefault("1")
    @Column(name = "active", nullable = false)
    private Boolean active;

    @OneToMany(mappedBy = "brand")
    private Set<Product> products = new LinkedHashSet<>();
}