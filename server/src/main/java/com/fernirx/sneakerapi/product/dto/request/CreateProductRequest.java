package com.fernirx.sneakerapi.product.dto.request;

import com.fernirx.sneakerapi.product.enums.ClosureType;
import com.fernirx.sneakerapi.product.enums.Gender;
import com.fernirx.sneakerapi.product.enums.ShaftStyle;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record CreateProductRequest(
        @NotNull
        Long brandId,

        @NotBlank(message = "{validation.field.not_blank}")
        @Size(max = 50, message = "{validation.size.max}")
        String code,

        @Size(max = 50, message = "{validation.size.max}")
        String styleCode,

        @NotBlank(message = "{validation.field.not_blank}")
        @Size(max = 255, message = "{validation.size.max}")
        String name,

        @NotNull
        Gender gender,

        String description,

        @Size(max = 100, message = "{validation.size.max}")
        String upperMaterial,

        @Size(max = 100, message = "{validation.size.max}")
        String soleType,

        ClosureType closureType,

        ShaftStyle shaftStyle,

        @NotNull
        BigDecimal basePrice,

        BigDecimal originalPrice,

        BigDecimal costPrice,

        Boolean newArrival,

        Boolean onSale
) {}
