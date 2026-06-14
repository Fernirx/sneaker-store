package com.fernirx.sneakerapi.product.dto.request;

import com.fernirx.sneakerapi.common.annotation.NullableNotBlank;
import com.fernirx.sneakerapi.product.enums.ClosureType;
import com.fernirx.sneakerapi.product.enums.Gender;
import com.fernirx.sneakerapi.product.enums.ShaftStyle;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record UpdateProductRequest(
        Long brandId,

        @NullableNotBlank
        @Size(max = 50, message = "{validation.size.max}")
        String code,

        @Size(max = 50, message = "{validation.size.max}")
        String styleCode,

        @NullableNotBlank
        @Size(max = 255, message = "{validation.size.max}")
        String name,

        Gender gender,

        @NullableNotBlank
        String description,

        @Size(max = 100, message = "{validation.size.max}")
        String upperMaterial,

        @Size(max = 100, message = "{validation.size.max}")
        String soleType,

        ClosureType closureType,

        ShaftStyle shaftStyle,

        BigDecimal basePrice,

        BigDecimal originalPrice,

        BigDecimal costPrice,

        Boolean newArrival,

        Boolean onSale,

        Boolean active
) {}
