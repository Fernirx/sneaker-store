package com.fernirx.sneakerapi.user.dto.request;

import com.fernirx.sneakerapi.common.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.Set;

public record CreateUserRequest(
        @NotBlank(message = "{validation.field.not_blank}")
        @Email(message = "{validation.format.invalid}")
        @Size(max = 100, message = "{validation.size.max}")
        String email,

        @NotBlank(message = "{validation.field.not_blank}")
        @Size(max = 255, message = "{validation.size.max}")
        String password,

        @NotBlank(message = "{validation.field.not_blank}")
        @Size(max = 100, message = "{validation.size.max}")
        String firstName,

        Set<Role> roles
) {}
