package com.fernirx.sneakerapi.supplier.dto.request;

import com.fernirx.sneakerapi.common.annotation.NullableNotBlank;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record ReceivePurchaseItemRequest(
        @NotNull(message = "{validation.field.not_blank}")
        Long purchaseItemId,

        @NotNull(message = "{validation.field.not_blank}")
        @Min(value = 0, message = "{validation.number.min}")
        Integer quantityReceived,

        @NotNull(message = "{validation.field.not_blank}")
        @Min(value = 0, message = "{validation.number.min}")
        Integer defectiveQty,

        @NullableNotBlank
        String note
) {}
