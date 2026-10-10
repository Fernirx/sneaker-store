package com.fernirx.sneakerapi.supplier.dto.request;

import com.fernirx.sneakerapi.common.annotation.NullableNotBlank;
import com.fernirx.sneakerapi.common.annotation.ValidPhone;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateSupplierRequest(
        @NotBlank(message = "{validation.field.not_blank}")
        @Size(max = 50, message = "{validation.size.max}")
        String code,

        @NotBlank(message = "{validation.field.not_blank}")
        @Size(max = 200, message = "{validation.size.max}")
        String name,

        @NullableNotBlank
        @Email(message = "{validation.format.invalid}")
        @Size(max = 100, message = "{validation.size.max}")
        String email,

        @ValidPhone(allowNull = true)
        String phone,

        @NullableNotBlank
        @Size(max = 100, message = "{validation.size.max}")
        String contactPerson,

        @ValidPhone(allowNull = true)
        String contactPhone,

        @NullableNotBlank
        String address,

        @NullableNotBlank
        String notes
) {}
