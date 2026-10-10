package com.fernirx.sneakerapi.review.entity;

import com.fernirx.sneakerapi.common.entity.BaseAuditEntity;
import com.fernirx.sneakerapi.order.entity.Order;
import com.fernirx.sneakerapi.product.entity.Product;
import com.fernirx.sneakerapi.user.entity.User;
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
@Table(name = "product_reviews", indexes = {
        @Index(name = "idx_reviews_product_rating",
                columnList = "product_id, rating"),
        @Index(name = "idx_reviews_product_approved",
                columnList = "product_id, approved"),
        @Index(name = "idx_reviews_user",
                columnList = "user_id"),
        @Index(name = "idx_reviews_order",
                columnList = "order_id")}, uniqueConstraints = {@UniqueConstraint(name = "user_product_UNIQUE",
        columnNames = {
                "user_id",
                "product_id"})})
public class ProductReview extends BaseAuditEntity {
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(name = "rating", columnDefinition = "tinyint UNSIGNED", nullable = false)
    private Short rating;

    @Size(max = 255)
    @Column(name = "title")
    private String title;

    @Column(name = "comment", columnDefinition = "TEXT")
    private String comment;

    @NotNull
    @ColumnDefault("0")
    @Column(name = "approved", nullable = false)
    private Boolean approved;

    @OneToMany(mappedBy = "review")
    private Set<ReviewImage> reviewImages = new LinkedHashSet<>();
}