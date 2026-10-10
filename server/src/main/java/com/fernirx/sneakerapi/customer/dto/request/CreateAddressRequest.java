package com.fernirx.sneakerapi.customer.dto.request;

import com.fernirx.sneakerapi.common.annotation.NullableNotBlank;
import com.fernirx.sneakerapi.common.annotation.ValidName;
import com.fernirx.sneakerapi.common.annotation.ValidPhone;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateAddressRequest(
        @NotBlank(message = "{validation.field.not_blank}")
        @Size(max = 200, message = "{validation.size.max}")
        @ValidName
        String name,

        @ValidPhone
        String phone,

        @NotBlank(message = "{validation.field.not_blank}")
        @Size(max = 255, message = "{validation.size.max}")
        String street,

        @NotBlank(message = "{validation.field.not_blank}")
        @Size(max = 100, message = "{validation.size.max}")
        String ward,

        @NotNull(message = "{validation.field.not_blank}")
        @Min(1)
        Integer wardCode,

        @NotBlank(message = "{validation.field.not_blank}")
        @Size(max = 100, message = "{validation.size.max}")
        String district,

        @NotNull(message = "{validation.field.not_blank}")
        @Min(1)
        Integer districtCode,

        @NotBlank(message = "{validation.field.not_blank}")
        @Size(max = 100, message = "{validation.size.max}")
        String province,

        @NotNull(message = "{validation.field.not_blank}")
        @Min(1)
        Integer provinceCode,

        @Size(max = 20, message = "{validation.size.max}")
        String postalCode,

        @NotNull(message = "{validation.field.not_blank}")
        Boolean defaultAddress
) {}
