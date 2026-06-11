package com.fernirx.sneakerapi.customer.dto.request;

import com.fernirx.sneakerapi.common.annotation.NullableNotBlank;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateAddressRequest(
        @NotBlank(message = "{validation.field.not_blank}")
        @Size(max = 200, message = "{validation.size.max}")
        String name,

        @NotBlank(message = "{validation.field.not_blank}")
        @Size(max = 20, message = "{validation.size.max}")
        String phone,

        @NotBlank(message = "{validation.field.not_blank}")
        @Size(max = 255, message = "{validation.size.max}")
        String street,

        @NullableNotBlank
        @Size(max = 100, message = "{validation.size.max}")
        String ward,

        @NotBlank(message = "{validation.field.not_blank}")
        @Size(max = 100, message = "{validation.size.max}")
        String district,

        @NotBlank(message = "{validation.field.not_blank}")
        @Size(max = 100, message = "{validation.size.max}")
        String province,

        @NullableNotBlank
        @Size(max = 20, message = "{validation.size.max}")
        String postalCode,

        @NotNull(message = "{validation.field.not_blank}")
        Boolean defaultAddress
) {}
