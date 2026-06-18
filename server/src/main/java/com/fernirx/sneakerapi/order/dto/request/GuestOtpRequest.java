package com.fernirx.sneakerapi.order.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record GuestOtpRequest(
        @NotBlank(message = "{validation.field.not_blank}")
        @Email(message = "{validation.format.invalid}")
        String email
) {}
