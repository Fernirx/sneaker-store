package com.fernirx.sneakerapi.category.entity;

import com.fernirx.sneakerapi.product.entity.ProductCategory;
import com.fernirx.sneakerapi.common.entity.BaseAuditEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "categories", indexes = {@Index(name = "idx_categories_parent_active",
        columnList = "parent_id, active")}, uniqueConstraints = {
        @UniqueConstraint(name = "name_UNIQUE",
                columnNames = {"name"}),
        @UniqueConstraint(name = "slug_UNIQUE",
                columnNames = {"slug"})})
public class Category extends BaseAuditEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.SET_NULL)
    @JoinColumn(name = "parent_id")
    private Category parent;

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
    @Column(name = "image_public_id")
    private String imagePublicId;

    @NotNull
    @ColumnDefault("0")
    @Column(name = "display_order", nullable = false)
    private Integer displayOrder;

    @NotNull
    @ColumnDefault("1")
    @Column(name = "active", nullable = false)
    private Boolean active;

    @OneToMany(mappedBy = "parent")
    private Set<Category> categories = new LinkedHashSet<>();

    @OneToMany(mappedBy = "category")
    private Set<CategoryTranslation> categoryTranslations = new LinkedHashSet<>();

    @OneToMany(mappedBy = "category")
    private Set<ProductCategory> productCategories = new LinkedHashSet<>();
}