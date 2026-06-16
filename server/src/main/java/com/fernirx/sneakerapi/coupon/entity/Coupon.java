package com.fernirx.sneakerapi.coupon.entity;

import com.fernirx.sneakerapi.common.entity.BaseAuditEntity;
import com.fernirx.sneakerapi.coupon.enums.DiscountType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "coupons", indexes = {@Index(name = "idx_coupons_active_dates",
        columnList = "active, start_date, end_date")}, uniqueConstraints = {@UniqueConstraint(name = "code_UNIQUE",
        columnNames = {"code"})})
public class Coupon extends BaseAuditEntity {
    @Size(max = 50)
    @NotNull
    @Column(name = "code", nullable = false, length = 50)
    private String code;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "discount_type", nullable = false)
    private DiscountType discountType;

    @NotNull
    @Column(name = "discount_value", nullable = false, precision = 15, scale = 2)
    private BigDecimal discountValue;

    @Column(name = "min_order_amount", precision = 15, scale = 2)
    private BigDecimal minOrderAmount;

    @Column(name = "max_discount_amount", precision = 15, scale = 2)
    private BigDecimal maxDiscountAmount;

    @Column(name = "usage_limit")
    private Integer usageLimit;

    @NotNull
    @ColumnDefault("0")
    @Column(name = "used_count", nullable = false)
    private Integer usedCount;

    @Column(name = "user_usage_limit")
    private Integer userUsageLimit;

    @NotNull
    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;

    @NotNull
    @Column(name = "end_date", nullable = false)
    private LocalDateTime endDate;

    @NotNull
    @ColumnDefault("1")
    @Column(name = "active", nullable = false)
    private Boolean active;

    @OneToMany(mappedBy = "coupon")
    private Set<CouponUsage> couponUsages = new LinkedHashSet<>();
}