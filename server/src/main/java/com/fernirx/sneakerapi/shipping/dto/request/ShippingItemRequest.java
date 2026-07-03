package com.fernirx.sneakerapi.shipping.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record ShippingItemRequest(
        @NotNull Long variantId,
        @NotNull @Min(1) Integer quantity
) {}
