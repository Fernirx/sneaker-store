package com.fernirx.sneakerapi.category.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record CategoryInternalResponse(
        Long id,
        String name,
        String slug,
        String description,
        String imagePublicId,
        Integer displayOrder,
        boolean active,
        Long parentId,
        String parentName,
        LocalDateTime createdAt,
        List<TranslationResponse> translations
) {}
