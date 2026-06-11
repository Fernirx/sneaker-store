package com.fernirx.sneakerapi.auth.dto.request;

import com.fernirx.sneakerapi.common.annotation.PasswordConfirmable;
import com.fernirx.sneakerapi.common.annotation.PasswordMatches;
import com.fernirx.sneakerapi.common.annotation.StrongPassword;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@PasswordMatches
public record ResetPasswordRequest(
        @NotBlank(message = "{validation.field.not_blank}")
        String resetToken,

        @NotBlank(message = "{validation.field.not_blank}")
        @Size(max = 255, message = "{validation.size.max}")
        @StrongPassword
        String password,

        @NotBlank(message = "{validation.field.not_blank}")
        @Size(max = 255, message = "{validation.size.max}")
        @StrongPassword
        String confirmPassword
) implements PasswordConfirmable {}