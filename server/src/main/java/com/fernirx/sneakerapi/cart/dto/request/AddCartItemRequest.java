package com.fernirx.sneakerapi.cart.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record AddCartItemRequest(
        @NotNull(message = "{validation.field.not_blank}")
        Long variantId,

        @NotNull(message = "{validation.field.not_blank}")
        @Min(value = 1, message = "{validation.quantity.min}")
        Integer quantity
) {}
