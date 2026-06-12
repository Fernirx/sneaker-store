package com.fernirx.sneakerapi.category.dto.request;

import com.fernirx.sneakerapi.common.annotation.NullableNotBlank;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TranslationRequest(
        @NotBlank(message = "{validation.field.not_blank}")
        @Size(max = 10, message = "{validation.size.max}")
        String locale,

        @NotBlank(message = "{validation.field.not_blank}")
        @Size(max = 100, message = "{validation.size.max}")
        String name,

        @NullableNotBlank
        String description
) {}
