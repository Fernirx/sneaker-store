package com.fernirx.sneakerapi.inventory.dto.response;

public record StockAdjustmentItemResponse(
        Long id,
        Long variantId,
        String sku,
        String productName,
        Short size,
        String colorway,
        Integer quantityChange,
        Integer quantityBefore,
        Integer quantityAfter,
        String note
) {}
