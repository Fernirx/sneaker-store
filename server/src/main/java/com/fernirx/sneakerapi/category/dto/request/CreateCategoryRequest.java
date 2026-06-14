package com.fernirx.sneakerapi.category.dto.request;

import com.fernirx.sneakerapi.common.annotation.NullableNotBlank;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record CreateCategoryRequest(
        @NotBlank(message = "{validation.field.not_blank}")
        @Size(max = 100, message = "{validation.size.max}")
        String name,

        @NullableNotBlank
        String description,

        @NullableNotBlank
        @Size(max = 255, message = "{validation.size.max}")
        String imagePublicId,

        @NotNull(message = "{validation.field.not_blank}")
        Integer displayOrder,

        Long parentId,

        List<TranslationRequest> translations
) {}
