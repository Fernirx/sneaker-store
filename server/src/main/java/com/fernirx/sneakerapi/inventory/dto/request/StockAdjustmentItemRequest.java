package com.fernirx.sneakerapi.inventory.dto.request;

import com.fernirx.sneakerapi.common.annotation.NullableNotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record StockAdjustmentItemRequest(
        @NotNull(message = "{validation.field.not_blank}")
        Long variantId,

        @NotNull(message = "{validation.field.not_blank}")
        Integer quantityChange,

        @NullableNotBlank
        @Size(max = 255, message = "{validation.size.max}")
        String note
) {}
