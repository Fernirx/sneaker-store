package com.fernirx.sneakerapi.coupon.dto.response;

import java.math.BigDecimal;

public record CouponApplyResult(
        Long couponId,
        String code,
        BigDecimal discountAmount
) {}
