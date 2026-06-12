package com.fernirx.sneakerapi.brand.dto.request;

public record BrandFilterRequest(
        String search,
        Boolean active
) {}
