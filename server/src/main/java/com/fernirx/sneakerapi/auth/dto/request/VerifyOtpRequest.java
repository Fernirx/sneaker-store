package com.fernirx.sneakerapi.auth.dto.request;

import com.fernirx.sneakerapi.common.constant.PatternConstants;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record VerifyOtpRequest(
        @NotBlank(message = "{validation.field.not_blank}")
        @Size(max = 100, message = "{validation.size.max}")
        @Email(message = "{validation.format.invalid}")
        String email,

        @NotBlank(message = "{validation.field.not_blank}")
        @Pattern(regexp = PatternConstants.OTP, message = "{validation.format.invalid}")
        String otp
) {}