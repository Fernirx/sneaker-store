package com.fernirx.sneakerapi.auth.dto.request;

import com.fernirx.sneakerapi.user.enums.OtpPurpose;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ResendOtpRequest(
        @NotBlank(message = "{validation.field.not_blank}")
        @Size(max = 100, message = "{validation.size.max}")
        @Email(message = "{validation.format.invalid}")
        String email,

        @NotNull(message = "{validation.field.not_blank}")
        OtpPurpose purpose
) {}