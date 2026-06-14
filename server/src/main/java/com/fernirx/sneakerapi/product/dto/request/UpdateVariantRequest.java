package com.fernirx.sneakerapi.product.dto.request;

import com.fernirx.sneakerapi.common.annotation.NullableNotBlank;
import com.fernirx.sneakerapi.product.enums.ShoeWidth;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record UpdateVariantRequest(
        Short size,

        ShoeWidth shoeWidth,

        @NullableNotBlank
        @Size(max = 100, message = "{validation.size.max}")
        String colorway,

        @Size(max = 50, message = "{validation.size.max}")
        String colorwayCode,

        @Size(max = 7, message = "{validation.size.max}")
        String colorHex,

        BigDecimal price,

        @NullableNotBlank
        @Size(max = 100, message = "{validation.size.max}")
        String sku,

        Integer stockQuantity,

        Integer minStockLevel,

        Integer displayOrder,

        Boolean active
) {}
