package com.fernirx.sneakerapi.auth.dto.request;

import jakarta.validation.constraints.NotBlank;

public record LogoutRequest(
        @NotBlank(message = "{validation.field.not_blank}")
        String accessToken,

        @NotBlank(message = "{validation.field.not_blank}")
        String refreshToken
) {}