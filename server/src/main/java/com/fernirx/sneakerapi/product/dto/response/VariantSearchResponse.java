package com.fernirx.sneakerapi.product.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fernirx.sneakerapi.product.enums.ShoeWidth;

import java.math.BigDecimal;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record VariantSearchResponse(
        Long id,
        String sku,
        Long productId,
        String productName,
        String productCode,
        String colorway,
        String colorwayCode,
        String colorHex,
        Short size,
        ShoeWidth shoeWidth,
        BigDecimal price,

        Integer stockQuantity,
        Boolean active
) {}
