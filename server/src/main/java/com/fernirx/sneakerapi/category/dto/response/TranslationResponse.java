package com.fernirx.sneakerapi.category.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record TranslationResponse(
        String locale,
        String name,
        String description
) {}
