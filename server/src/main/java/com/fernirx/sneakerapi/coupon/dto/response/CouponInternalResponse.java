package com.fernirx.sneakerapi.coupon.dto.response;

import com.fernirx.sneakerapi.coupon.enums.DiscountType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CouponInternalResponse(
        Long id,
        String code,
        String description,
        DiscountType discountType,
        BigDecimal discountValue,
        BigDecimal minOrderAmount,
        BigDecimal maxDiscountAmount,
        Integer usageLimit,
        Integer usedCount,
        Integer userUsageLimit,
        LocalDateTime startDate,
        LocalDateTime endDate,
        Boolean active,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
