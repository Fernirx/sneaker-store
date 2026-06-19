package com.fernirx.sneakerapi.inventory.dto.request;

import com.fernirx.sneakerapi.common.annotation.NullableNotBlank;

public record CancelStockAdjustmentRequest(
        @NullableNotBlank
        String reason
) {}
