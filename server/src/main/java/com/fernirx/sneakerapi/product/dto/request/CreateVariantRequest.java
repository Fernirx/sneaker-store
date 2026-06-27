package com.fernirx.sneakerapi.product.dto.request;

import com.fernirx.sneakerapi.common.annotation.NullableNotBlank;
import com.fernirx.sneakerapi.product.enums.ShoeWidth;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record CreateVariantRequest(
        @NotNull(message = "{validation.field.not_blank}")
        Short size,

        @NotNull(message = "{validation.field.not_blank}")
        ShoeWidth shoeWidth,

        @NotBlank(message = "{validation.field.not_blank}")
        @Size(max = 100, message = "{validation.size.max}")
        String colorway,

        @NullableNotBlank
        @Size(max = 50, message = "{validation.size.max}")
        String colorwayCode,

        @NullableNotBlank
        @Size(max = 7, message = "{validation.size.max}")
        String colorHex,

        BigDecimal price,

        BigDecimal originalPrice,

        BigDecimal costPrice,

        @NotBlank(message = "{validation.field.not_blank}")
        @Size(max = 100, message = "{validation.size.max}")
        String sku,

        @NotNull(message = "{validation.field.not_blank}")
        Integer stockQuantity,

        Integer minStockLevel,

        Integer displayOrder,

        Boolean active
) {}
