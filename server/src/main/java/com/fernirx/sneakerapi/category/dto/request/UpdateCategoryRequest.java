package com.fernirx.sneakerapi.category.dto.request;

import com.fernirx.sneakerapi.common.annotation.NullableNotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

public record UpdateCategoryRequest(
        @NullableNotBlank
        @Size(max = 100, message = "{validation.size.max}")
        String name,

        @NullableNotBlank
        String description,

        @NullableNotBlank
        @Size(max = 255, message = "{validation.size.max}")
        String imagePublicId,

        Integer displayOrder,

        Boolean active,

        List<TranslationRequest> translations
) {}
