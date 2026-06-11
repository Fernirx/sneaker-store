package com.fernirx.sneakerapi.customer.dto.request;

import com.fernirx.sneakerapi.common.annotation.NullableNotBlank;
import jakarta.validation.constraints.Size;

public record UpdateAddressRequest(
        @NullableNotBlank
        @Size(max = 200, message = "{validation.size.max}")
        String name,

        @NullableNotBlank
        @Size(max = 20, message = "{validation.size.max}")
        String phone,

        @NullableNotBlank
        @Size(max = 255, message = "{validation.size.max}")
        String street,

        @NullableNotBlank
        @Size(max = 100, message = "{validation.size.max}")
        String ward,

        @NullableNotBlank
        @Size(max = 100, message = "{validation.size.max}")
        String district,

        @NullableNotBlank
        @Size(max = 100, message = "{validation.size.max}")
        String province,

        @NullableNotBlank
        @Size(max = 20, message = "{validation.size.max}")
        String postalCode,

        Boolean defaultAddress
) {}
