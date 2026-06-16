package com.fernirx.sneakerapi.coupon.dto.request;

import com.fernirx.sneakerapi.coupon.enums.DiscountType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CreateCouponRequest(
        @NotBlank(message = "{validation.field.not_blank}")
        @Size(max = 50, message = "{validation.size.max}")
        String code,

        String description,

        @NotNull(message = "{validation.field.not_blank}")
        DiscountType discountType,

        @NotNull(message = "{validation.field.not_blank}")
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

        @NotNull(message = "{validation.field.not_blank}")
        LocalDateTime startDate,

        @NotNull(message = "{validation.field.not_blank}")
        LocalDateTime endDate
) {}
