package com.fernirx.sneakerapi.product.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fernirx.sneakerapi.product.enums.ClosureType;
import com.fernirx.sneakerapi.product.enums.Gender;
import com.fernirx.sneakerapi.product.enums.ShaftStyle;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ProductInternalResponse(
        Long id,
        String slug,
        String code,
        String styleCode,
        String name,
        Gender gender,
        String description,
        String upperMaterial,
        String soleType,
        ClosureType closureType,
        ShaftStyle shaftStyle,
        BigDecimal basePrice,
        BigDecimal originalPrice,
        BigDecimal costPrice,
        Boolean newArrival,
        Boolean onSale,
        Boolean active,
        Integer soldCount,
        Integer viewCount,
        BrandInfo brand,
        String primaryImagePublicId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public record BrandInfo(Long id, String name, String slug, String logoPublicId) {}
}
