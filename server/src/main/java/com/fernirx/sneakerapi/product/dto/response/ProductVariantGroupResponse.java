package com.fernirx.sneakerapi.product.dto.response;

import com.fernirx.sneakerapi.product.enums.ShoeWidth;

import java.math.BigDecimal;
import java.util.List;

public record ProductVariantGroupResponse(
        String colorway,
        String colorwayCode,
        String colorHex,
        List<VariantResponse> variants
) {
    public record VariantResponse(
            Long id,
            Short size,
            ShoeWidth shoeWidth,
            String sku,
            BigDecimal price,
            Integer stockQuantity,
            Integer minStockLevel,
            Integer displayOrder,
            Boolean active
    ) {}
}
