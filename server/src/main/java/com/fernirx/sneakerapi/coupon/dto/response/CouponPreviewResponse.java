package com.fernirx.sneakerapi.coupon.dto.response;

import com.fernirx.sneakerapi.coupon.enums.DiscountType;

import java.math.BigDecimal;

public record CouponPreviewResponse(
        String code,
        DiscountType discountType,
        BigDecimal discountValue,
        BigDecimal minOrderAmount,
        BigDecimal maxDiscountAmount,
        BigDecimal discountAmount,
        BigDecimal finalAmount
) {}
