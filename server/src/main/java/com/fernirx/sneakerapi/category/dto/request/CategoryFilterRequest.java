package com.fernirx.sneakerapi.category.dto.request;

public record CategoryFilterRequest(
        String search,
        Boolean active,
        Long parentId
) {}
