package com.fernirx.sneakerapi.category.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record CategoryResponse(
        Long id,
        String name,
        String slug,
        String description,
        String imagePublicId,
        Integer displayOrder,
        Long parentId,
        List<TranslationResponse> translations
) {}
