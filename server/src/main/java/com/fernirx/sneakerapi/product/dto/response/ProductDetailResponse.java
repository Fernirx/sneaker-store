package com.fernirx.sneakerapi.product.dto.response;

import com.fernirx.sneakerapi.product.enums.ClosureType;
import com.fernirx.sneakerapi.product.enums.Gender;
import com.fernirx.sneakerapi.product.enums.ShaftStyle;
import com.fernirx.sneakerapi.product.enums.ShoeWidth;

import java.math.BigDecimal;
import java.util.List;

public record ProductDetailResponse(
        Long id,
        String slug,
        String name,
        String description,
        Gender gender,
        String upperMaterial,
        String soleType,
        ClosureType closureType,
        ShaftStyle shaftStyle,
        BigDecimal minPrice,
        BigDecimal maxPrice,
        Boolean newArrival,
        Boolean onSale,
        Integer soldCount,
        Integer viewCount,
        BrandInfo brand,
        List<ColorDetailResponse> colors
) {
    public record BrandInfo(
            Long id,
            String name,
            String slug,
            String logoPublicId,
            String description
    ) {}

    public record ColorDetailResponse(
            String colorway,
            String colorwayCode,
            String colorHex,
            List<ImageResponse> images,
            List<SizeResponse> sizes
    ) {}

    public record ImageResponse(
            String publicId,
            boolean primary
    ) {}

    public record SizeResponse(
            Long variantId,
            String sku,
            Short size,
            ShoeWidth shoeWidth,
            BigDecimal price,
            BigDecimal originalPrice,
            Integer stockQuantity
    ) {}
}
