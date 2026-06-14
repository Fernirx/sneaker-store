package com.fernirx.sneakerapi.cart.dto.request;

import jakarta.validation.constraints.NotBlank;

public record MergeCartRequest(
        @NotBlank(message = "{validation.field.not_blank}")
        String guestToken
) {}
