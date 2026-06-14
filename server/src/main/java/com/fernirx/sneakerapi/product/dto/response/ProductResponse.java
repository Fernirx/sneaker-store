package com.fernirx.sneakerapi.product.dto.response;

import com.fernirx.sneakerapi.product.enums.Gender;

import java.math.BigDecimal;
import java.util.List;

public record ProductResponse(
        Long id,
        String slug,
        String name,
        Gender gender,
        BigDecimal basePrice,
        BigDecimal originalPrice,
        Boolean newArrival,
        Boolean onSale,
        BrandInfo brand,
        List<ColorSwatchResponse> colors
) {
    public record BrandInfo(
            Long id,
            String name,
            String slug,
            String logoPublicId
    ) {}

    public record ColorSwatchResponse(
            String colorway,
            String colorwayCode,
            String colorHex,
            String primaryImagePublicId,
            BigDecimal price
    ) {}
}
