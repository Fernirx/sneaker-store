package com.fernirx.sneakerapi.setting.entity;

import com.fernirx.sneakerapi.common.entity.BaseAuditEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Entity
@Table(name = "store_settings")
public class StoreSetting extends BaseAuditEntity {
    @NotNull
    @Positive
    @Column(name = "points_per_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal pointsPerAmount;

    @NotNull
    @Positive
    @Column(name = "free_ship_threshold", nullable = false, precision = 15, scale = 2)
    private BigDecimal freeShipThreshold;

    @NotNull
    @Positive
    @Column(name = "silver_threshold", nullable = false, precision = 15, scale = 2)
    private BigDecimal silverThreshold;

    @NotNull
    @Positive
    @Column(name = "gold_threshold", nullable = false, precision = 15, scale = 2)
    private BigDecimal goldThreshold;

    @NotNull
    @Positive
    @Column(name = "platinum_threshold", nullable = false, precision = 15, scale = 2)
    private BigDecimal platinumThreshold;

    @NotNull
    @Column(name = "silver_discount_rate", nullable = false)
    private Integer silverDiscountRate;

    @NotNull
    @Column(name = "gold_discount_rate", nullable = false)
    private Integer goldDiscountRate;

    @NotNull
    @Column(name = "platinum_discount_rate", nullable = false)
    private Integer platinumDiscountRate;
}
