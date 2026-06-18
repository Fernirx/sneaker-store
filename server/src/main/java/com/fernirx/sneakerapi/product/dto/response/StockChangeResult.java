package com.fernirx.sneakerapi.product.dto.response;

public record StockChangeResult(
        Long variantId,
        int oldStock,
        int newStock
) {}
