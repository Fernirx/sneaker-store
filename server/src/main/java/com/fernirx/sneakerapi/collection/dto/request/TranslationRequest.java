package com.fernirx.sneakerapi.collection.dto.request;

import com.fernirx.sneakerapi.common.annotation.NullableNotBlank;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TranslationRequest(
        @NotBlank @Size(max = 10) String locale,
        @NotBlank @Size(max = 100) String name,
        @NullableNotBlank String description
) {}
