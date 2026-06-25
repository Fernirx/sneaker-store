package com.fernirx.sneakerapi.customer.dto.request;

import jakarta.validation.constraints.NotNull;

public record CreateWishlistRequest(
        @NotNull(message = "{validation.field.not_blank}")
        Long productId,

        Long variantId
) {}
