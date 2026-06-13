package com.fernirx.sneakerapi.product.dto.request;

import com.fernirx.sneakerapi.product.enums.ShoeWidth;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record CreateVariantRequest(
        @NotNull
        Short size,

        @NotNull
        ShoeWidth shoeWidth,

        @NotBlank(message = "{validation.field.not_blank}")
        @Size(max = 100, message = "{validation.size.max}")
        String colorway,

        @Size(max = 50, message = "{validation.size.max}")
        String colorwayCode,

        @Size(max = 7, message = "{validation.size.max}")
        String colorHex,

        BigDecimal price,

        @NotBlank(message = "{validation.field.not_blank}")
        @Size(max = 100, message = "{validation.size.max}")
        String sku,

        @NotNull
        Integer stockQuantity,

        Integer minStockLevel,

        Integer displayOrder
) {}
