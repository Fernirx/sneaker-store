package com.fernirx.sneakerapi.returns.dto.response;

import java.math.BigDecimal;

public record ReturnRequestItemResponse(
        Long id,
        Long orderItemId,
        String productName,
        String variantSku,
        Short variantSize,
        String variantColor,
        Integer quantity,
        BigDecimal unitPrice,
        Long exchangeVariantId,
        String exchangeVariantSku,
        Short exchangeVariantSize,
        String exchangeVariantColorway,
        BigDecimal refundAmount
) {}
