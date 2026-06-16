package com.fernirx.sneakerapi.coupon.entity;

import com.fernirx.sneakerapi.common.entity.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "coupon_usages", indexes = {
        @Index(name = "idx_coupon_usages_coupon_email",
                columnList = "coupon_id, email"),
        @Index(name = "idx_coupon_usages_coupon_phone",
                columnList = "coupon_id, phone")}, uniqueConstraints = {@UniqueConstraint(name = "order_id_UNIQUE",
        columnNames = {"order_id"})})
public class CouponUsage extends BaseEntity {
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "coupon_id", nullable = false)
    private Coupon coupon;

    @Size(max = 100)
    @Column(name = "email", length = 100)
    private String email;

    @Size(max = 20)
    @Column(name = "phone", length = 20)
    private String phone;

    @NotNull
    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "used_at", nullable = false)
    private LocalDateTime usedAt;
}