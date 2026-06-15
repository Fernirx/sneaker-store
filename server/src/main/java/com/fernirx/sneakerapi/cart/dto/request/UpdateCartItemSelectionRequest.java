package com.fernirx.sneakerapi.cart.dto.request;

import jakarta.validation.constraints.NotNull;

public record UpdateCartItemSelectionRequest(
        @NotNull Boolean selected
) {}
