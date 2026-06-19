package com.fernirx.sneakerapi.supplier.dto.request;

import com.fernirx.sneakerapi.common.annotation.NullableNotBlank;

public record CancelPurchaseRequest(
        @NullableNotBlank
        String reason
) {}
