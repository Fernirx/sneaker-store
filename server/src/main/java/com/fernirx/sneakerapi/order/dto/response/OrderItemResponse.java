package com.fernirx.sneakerapi.order.dto.response;

import java.math.BigDecimal;

public record OrderItemResponse(
        Long id,
        Long variantId,
        String productCode,
        String productName,
        String variantSku,
        Short variantSize,
        String variantColor,
        Integer quantity,
        BigDecimal originalPrice,
        BigDecimal unitPrice,
        BigDecimal subtotal
) {}
