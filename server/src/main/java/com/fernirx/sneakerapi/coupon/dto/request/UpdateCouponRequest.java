package com.fernirx.sneakerapi.coupon.dto.request;

import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record UpdateCouponRequest(
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
