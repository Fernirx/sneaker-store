package com.fernirx.sneakerapi.coupon.dto.request;

import com.fernirx.sneakerapi.common.annotation.NullableNotBlank;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record UpdateCouponRequest(
        @NullableNotBlank
        String description,

        @Positive
        BigDecimal discountValue,

        @Positive
        BigDecimal minOrderAmount,

        @Positive
        BigDecimal maxDiscountAmount,

        @Positive
        Integer usageLimit,

        @Positive
        Integer userUsageLimit,

        LocalDateTime startDate,

        LocalDateTime endDate,

        Boolean active
) {}
