package com.fernirx.sneakerapi.user.dto.request;

import com.fernirx.sneakerapi.common.annotation.PasswordConfirmable;
import com.fernirx.sneakerapi.common.annotation.PasswordMatches;
import com.fernirx.sneakerapi.common.annotation.StrongPassword;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@PasswordMatches
public record ChangePasswordRequest(
        @NotBlank(message = "{validation.field.not_blank}")
        @Size(max = 255, message = "{validation.size.max}")
        String currentPassword,

        @NotBlank(message = "{validation.field.not_blank}")
        @Size(max = 255, message = "{validation.size.max}")
        @StrongPassword
        String password,

        @NotBlank(message = "{validation.field.not_blank}")
        @Size(max = 255, message = "{validation.size.max}")
        String confirmPassword
) implements PasswordConfirmable {}
