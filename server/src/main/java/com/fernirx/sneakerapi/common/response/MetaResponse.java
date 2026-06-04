package com.fernirx.sneakerapi.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record MetaResponse(
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean last
) {}
