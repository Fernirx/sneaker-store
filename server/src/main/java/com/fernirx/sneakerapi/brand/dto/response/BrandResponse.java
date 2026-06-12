package com.fernirx.sneakerapi.brand.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record BrandResponse(
        Long id,
        String name,
        String slug,
        String description,
        String logoPublicId
) {}
