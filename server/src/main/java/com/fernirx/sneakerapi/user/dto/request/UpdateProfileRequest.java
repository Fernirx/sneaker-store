package com.fernirx.sneakerapi.user.dto.request;

import com.fernirx.sneakerapi.common.annotation.NullableNotBlank;
import com.fernirx.sneakerapi.common.annotation.ValidName;
import com.fernirx.sneakerapi.common.annotation.ValidPhone;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record UpdateProfileRequest(
        @NullableNotBlank
        @Size(max = 100, message = "{validation.size.max}")
        @ValidName(allowNull = true)
        String firstName,

        @Size(max = 100, message = "{validation.size.max}")
        @ValidName(allowNull = true)
        String lastName,

        @ValidPhone(allowNull = true)
        String phone,

        @Past(message = "{validation.date.past}")
        LocalDate dateOfBirth,

        @NullableNotBlank
        @Size(max = 255, message = "{validation.size.max}")
        String avatarPublicId
) {}