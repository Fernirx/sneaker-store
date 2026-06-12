package com.fernirx.sneakerapi.brand.dto.request;

import com.fernirx.sneakerapi.common.annotation.NullableNotBlank;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateBrandRequest(
        @NotBlank(message = "{validation.field.not_blank}")
        @Size(max = 100, message = "{validation.size.max}")
        String name,

        @NullableNotBlank
        String description,

        @NullableNotBlank
        @Size(max = 255, message = "{validation.size.max}")
        String logoPublicId
) {}
