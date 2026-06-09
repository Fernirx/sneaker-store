package com.fernirx.sneakerapi.auth.dto.request;

import com.fernirx.sneakerapi.common.annotation.NullableNotBlank;
import com.fernirx.sneakerapi.common.annotation.PasswordConfirmable;
import com.fernirx.sneakerapi.common.annotation.PasswordMatches;
import com.fernirx.sneakerapi.common.annotation.StrongPassword;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@PasswordMatches
public record RegisterRequest(
        @NotBlank(message = "{validation.field.not_blank}")
        @Size(max = 100, message = "{validation.size.max}")
        @Email(message = "{validation.format.invalid}")
        String email,

        @NotBlank(message = "{validation.field.not_blank}")
        @Size(max = 255, message = "{validation.size.max}")
        @StrongPassword
        String password,

        @NotBlank(message = "{validation.field.not_blank}")
        @Size(max = 255, message = "{validation.size.max}")
        @StrongPassword
        String confirmPassword,

        @NullableNotBlank
        @Size(max = 100, message = "{validation.size.max}")
        String firstName,

        @NullableNotBlank
        @Size(max = 100, message = "{validation.size.max}")
        String lastName
) implements PasswordConfirmable {}
