package com.fernirx.sneakerapi.setting.dto.request;

import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record UpdateStoreSettingRequest(
        @Positive(message = "{validation.number.positive}")
        BigDecimal pointsPerAmount,

        @Positive(message = "{validation.number.positive}")
        BigDecimal freeShipThreshold,

        @Positive(message = "{validation.number.positive}")
        BigDecimal silverThreshold,

        @Positive(message = "{validation.number.positive}")
        BigDecimal goldThreshold,

        @Positive(message = "{validation.number.positive}")
        BigDecimal platinumThreshold
) {}
