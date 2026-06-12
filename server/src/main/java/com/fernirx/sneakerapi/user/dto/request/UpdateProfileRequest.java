package com.fernirx.sneakerapi.user.dto.request;

import com.fernirx.sneakerapi.common.annotation.NullableNotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record UpdateProfileRequest(
        @NullableNotBlank
        @Size(max = 100, message = "{validation.size.max}")
        String firstName,

        @NullableNotBlank
        @Size(max = 100, message = "{validation.size.max}")
        String lastName,

        @NullableNotBlank
        @Size(max = 20, message = "{validation.size.max}")
        String phone,

        @Past(message = "{validation.date.past}")
        LocalDate dateOfBirth,

        @NullableNotBlank
        @Size(max = 255, message = "{validation.size.max}")
        String avatarPublicId
) {}