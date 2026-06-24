package com.fernirx.sneakerapi.review.entity;

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
@Table(name = "review_images", indexes = {@Index(name = "idx_review_images_review",
        columnList = "review_id")}, uniqueConstraints = {@UniqueConstraint(name = "image_public_id_UNIQUE",
        columnNames = {"image_public_id"})})
public class ReviewImage extends BaseCreatedEntity {
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "review_id", nullable = false)
    private ProductReview review;

    @Size(max = 255)
    @NotNull
    @Column(name = "image_public_id", nullable = false)
    private String imagePublicId;

    @NotNull
    @ColumnDefault("0")
    @Column(name = "display_order", nullable = false)
    private Integer displayOrder;
}