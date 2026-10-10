package com.fernirx.sneakerapi.coupon.dto.request;

import com.fernirx.sneakerapi.coupon.enums.DiscountType;

public record CouponFilterRequest(
        String search,
        Boolean active,
        DiscountType discountType
) {}
