package com.fernirx.sneakerapi.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fernirx.sneakerapi.common.enums.ErrorCode;

import java.time.Instant;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
        ErrorCode code,
        String message,
        List<ErrorDetail> fields,
        Instant timestamp
) {
    public static ErrorResponse of(ErrorCode code, String message) {
        return new ErrorResponse(code, message, null, Instant.now());
    }

    public static ErrorResponse of(ErrorCode code, String message, List<ErrorDetail> fields) {
        return new ErrorResponse(code, message, fields, Instant.now());
    }
}
