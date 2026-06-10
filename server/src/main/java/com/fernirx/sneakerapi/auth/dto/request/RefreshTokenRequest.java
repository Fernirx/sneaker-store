package com.fernirx.sneakerapi.auth.dto.request;

import jakarta.validation.constraints.NotBlank;

public record RefreshTokenRequest(
         @NotBlank(message = "{validation.field.not_blank}")
         String refreshToken
) {}
