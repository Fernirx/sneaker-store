package com.fernirx.sneakerapi.coupon.dto.request;

import com.fernirx.sneakerapi.common.annotation.NullableNotBlank;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record UpdateCouponRequest(
        @NullableNotBlank
        String description,

        @Positive(message = "{validation.number.positive}")
        BigDecimal discountValue,

        @Positive(message = "{validation.number.positive}")
        BigDecimal minOrderAmount,

        @Positive(message = "{validation.number.positive}")
        BigDecimal maxDiscountAmount,

        @Positive(message = "{validation.number.positive}")
        Integer usageLimit,

        @Positive(message = "{validation.number.positive}")
        Integer userUsageLimit,

        LocalDateTime startDate,

        LocalDateTime endDate,

        Boolean active
) {}
