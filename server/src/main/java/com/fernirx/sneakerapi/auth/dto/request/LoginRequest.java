package com.fernirx.sneakerapi.auth.dto.request;

import com.fernirx.sneakerapi.common.annotation.StrongPassword;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(
        @NotBlank(message = "{validation.field.not_blank}")
        @Size(max = 100, message = "{validation.size.max}")
        @Email(message = "{validation.format.invalid}")
        String email,

        @NotBlank(message = "{validation.field.not_blank}")
        @Size(max = 255, message = "{validation.size.max}")
        @StrongPassword
        String password
) {}
