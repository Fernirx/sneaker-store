package com.fernirx.sneakerapi.review.entity;

import com.fernirx.sneakerapi.common.entity.BaseAuditEntity;
import com.fernirx.sneakerapi.product.entity.Product;
import com.fernirx.sneakerapi.user.entity.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
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
@Table(name = "product_comments", indexes = {
        @Index(name = "idx_comments_product_approved",
                columnList = "product_id, approved"),
        @Index(name = "idx_comments_user",
                columnList = "user_id"),
        @Index(name = "idx_comments_parent",
                columnList = "parent_id")})
public class ProductComment extends BaseAuditEntity {
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

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "parent_id")
    private ProductComment parent;

    @NotNull
    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @NotNull
    @ColumnDefault("0")
    @Column(name = "approved", nullable = false)
    private Boolean approved;

    @OneToMany(mappedBy = "parent")
    private Set<ProductComment> productComments = new LinkedHashSet<>();
}