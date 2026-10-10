package com.fernirx.sneakerapi.returns.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record ReturnItemRequest(
        @NotNull(message = "{validation.field.not_blank}")
        Long orderItemId,

        @NotNull(message = "{validation.field.not_blank}")
        @Min(value = 1, message = "{validation.quantity.min}")
        Integer quantity,

        Long exchangeVariantId
) {}
