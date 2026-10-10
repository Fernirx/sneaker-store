package com.fernirx.sneakerapi.inventory.dto.request;

import com.fernirx.sneakerapi.common.annotation.NullableNotBlank;
import com.fernirx.sneakerapi.inventory.enums.StockAdjustmentType;
import jakarta.validation.Valid;

import java.util.List;

public record UpdateStockAdjustmentRequest(
        StockAdjustmentType type,

        @NullableNotBlank
        String reason,

        @Valid
        List<StockAdjustmentItemRequest> items
) {}
