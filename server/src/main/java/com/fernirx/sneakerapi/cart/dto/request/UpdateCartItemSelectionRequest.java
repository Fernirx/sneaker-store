package com.fernirx.sneakerapi.cart.dto.request;

import jakarta.validation.constraints.NotNull;

public record UpdateCartItemSelectionRequest(
        @NotNull(message = "{validation.field.not_blank}") Boolean selected
) {}
