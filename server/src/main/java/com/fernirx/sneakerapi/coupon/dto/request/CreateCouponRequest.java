package com.fernirx.sneakerapi.coupon.dto.request;

import com.fernirx.sneakerapi.common.annotation.NullableNotBlank;
import com.fernirx.sneakerapi.coupon.enums.DiscountType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.fernirx.sneakerapi.common.annotation.ValidDateRange;

@ValidDateRange(startField = "startDate", endField = "endDate")
public record CreateCouponRequest(
        @NotBlank(message = "{validation.field.not_blank}")
        @Size(max = 50, message = "{validation.size.max}")
        String code,

        @NullableNotBlank
        String description,

        @NotNull(message = "{validation.field.not_blank}")
        DiscountType discountType,

        @NotNull(message = "{validation.field.not_blank}")
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

        @NotNull(message = "{validation.field.not_blank}")
        LocalDateTime startDate,

        @NotNull(message = "{validation.field.not_blank}")
        LocalDateTime endDate
) {}
