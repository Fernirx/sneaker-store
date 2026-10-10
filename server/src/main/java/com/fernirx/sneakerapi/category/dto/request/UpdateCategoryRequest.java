package com.fernirx.sneakerapi.category.dto.request;

import com.fernirx.sneakerapi.common.annotation.NullableNotBlank;
import jakarta.validation.constraints.Size;

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

        Long parentId,

        Boolean clearParent,

        Boolean active
) {}
