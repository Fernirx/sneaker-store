package com.fernirx.sneakerapi.shipping.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ShippingFeeResponse(
        BigDecimal fee,
        LocalDateTime expectedDeliveryTime
) {}
