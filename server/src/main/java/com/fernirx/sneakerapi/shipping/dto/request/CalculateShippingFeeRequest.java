package com.fernirx.sneakerapi.shipping.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record CalculateShippingFeeRequest(
        @NotNull Integer toWardCode,
        String toAddress,
        @NotEmpty @Valid List<ShippingItemRequest> items
) {}
