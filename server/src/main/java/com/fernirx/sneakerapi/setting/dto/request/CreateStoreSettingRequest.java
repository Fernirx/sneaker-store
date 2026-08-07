package com.fernirx.sneakerapi.setting.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record CreateStoreSettingRequest(
        @NotNull(message = "{validation.field.not_blank}")
        @Positive(message = "{validation.number.positive}")
        BigDecimal pointsPerAmount,

        @NotNull(message = "{validation.field.not_blank}")
        @Positive(message = "{validation.number.positive}")
        BigDecimal freeShipThreshold,

        @NotNull(message = "{validation.field.not_blank}")
        @Positive(message = "{validation.number.positive}")
        BigDecimal silverThreshold,

        @NotNull(message = "{validation.field.not_blank}")
        @Positive(message = "{validation.number.positive}")
        BigDecimal goldThreshold,

        @NotNull(message = "{validation.field.not_blank}")
        @Positive(message = "{validation.number.positive}")
        BigDecimal platinumThreshold,

        @NotNull(message = "{validation.field.not_blank}")
        @jakarta.validation.constraints.Min(0)
        @jakarta.validation.constraints.Max(100)
        Integer silverDiscountRate,

        @NotNull(message = "{validation.field.not_blank}")
        @jakarta.validation.constraints.Min(0)
        @jakarta.validation.constraints.Max(100)
        Integer goldDiscountRate,

        @NotNull(message = "{validation.field.not_blank}")
        @jakarta.validation.constraints.Min(0)
        @jakarta.validation.constraints.Max(100)
        Integer platinumDiscountRate
) {}
