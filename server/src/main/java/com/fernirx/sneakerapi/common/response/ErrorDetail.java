package com.fernirx.sneakerapi.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorDetail(
        String field,
        String message
) {}
