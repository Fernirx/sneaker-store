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
        @NullableNotBlank
        @Size(max = 255, message = "{validation.size.max}")
        String name,

        Gender gender,

        @NullableNotBlank
        String description,

        @NullableNotBlank
        @Size(max = 100, message = "{validation.size.max}")
        String upperMaterial,

        @NullableNotBlank
        @Size(max = 100, message = "{validation.size.max}")
        String soleType,

        ClosureType closureType,

        ShaftStyle shaftStyle,



        Boolean newArrival,

        Boolean onSale,

        Boolean active
) {}
