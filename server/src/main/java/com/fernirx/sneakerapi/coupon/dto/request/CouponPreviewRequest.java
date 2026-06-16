package com.fernirx.sneakerapi.coupon.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record CouponPreviewRequest(
        @NotBlank(message = "{validation.field.not_blank}")
        String code,

        @NotNull(message = "{validation.field.not_blank}")
        @Positive
        BigDecimal orderAmount
) {}
