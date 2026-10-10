package com.fernirx.sneakerapi.setting.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record StoreSettingResponse(
        Long id,
        BigDecimal pointsPerAmount,
        BigDecimal freeShipThreshold,
        BigDecimal silverThreshold,
        BigDecimal goldThreshold,
        BigDecimal platinumThreshold,
        Integer silverDiscountRate,
        Integer goldDiscountRate,
        Integer platinumDiscountRate,
        LocalDateTime updatedAt
) {}
