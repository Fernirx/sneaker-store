package com.fernirx.sneakerapi.brand.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record BrandInternalResponse(
        Long id,
        String name,
        String slug,
        String description,
        String logoPublicId,
        boolean active,
        LocalDateTime createdAt
) {}
