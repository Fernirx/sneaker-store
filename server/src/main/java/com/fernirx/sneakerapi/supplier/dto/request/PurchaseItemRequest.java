package com.fernirx.sneakerapi.supplier.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record PurchaseItemRequest(
        @NotNull(message = "{validation.field.not_blank}")
        Long variantId,

        @NotNull(message = "{validation.field.not_blank}")
        @Min(value = 1, message = "{validation.quantity.min}")
        Integer quantityOrdered,

        @NotNull(message = "{validation.field.not_blank}")
        @DecimalMin(value = "0", message = "{validation.number.min}")
        BigDecimal unitCost
) {}
