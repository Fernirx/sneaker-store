package com.fernirx.sneakerapi.collection.entity;

import com.fernirx.sneakerapi.product.entity.ProductCollection;
import com.fernirx.sneakerapi.common.entity.BaseAuditEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "collections", indexes = {@Index(name = "idx_collections_active_dates",
        columnList = "active, launch_date, end_date")}, uniqueConstraints = {
        @UniqueConstraint(name = "name_UNIQUE",
                columnNames = {"name"}),
        @UniqueConstraint(name = "slug_UNIQUE",
                columnNames = {"slug"})})
public class Collection extends BaseAuditEntity {
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

    @Column(name = "launch_date")
    private LocalDate launchDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @NotNull
    @ColumnDefault("1")
    @Column(name = "active", nullable = false)
    private Boolean active;

    @OneToMany(mappedBy = "collection")
    private Set<CollectionTranslation> collectionTranslations = new LinkedHashSet<>();

    @OneToMany(mappedBy = "collection")
    private Set<ProductCollection> productCollections = new LinkedHashSet<>();
}