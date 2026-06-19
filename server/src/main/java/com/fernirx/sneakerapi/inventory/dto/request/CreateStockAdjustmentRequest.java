package com.fernirx.sneakerapi.inventory.dto.request;

import com.fernirx.sneakerapi.inventory.enums.StockAdjustmentType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record CreateStockAdjustmentRequest(
        @NotNull(message = "{validation.field.not_blank}")
        StockAdjustmentType type,

        @NotBlank(message = "{validation.field.not_blank}")
        String reason,

        @NotEmpty(message = "{validation.field.not_blank}")
        @Valid
        List<StockAdjustmentItemRequest> items
) {}
