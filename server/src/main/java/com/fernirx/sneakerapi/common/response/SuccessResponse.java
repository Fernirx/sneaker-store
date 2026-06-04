package com.fernirx.sneakerapi.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record SuccessResponse<T>(
        String message,
        T data,
        Instant timestamp
) {
    public static <T> SuccessResponse<T> of(T data) {
        return new SuccessResponse<>(null, data, Instant.now());
    }

    public static <T> SuccessResponse<T> of(String message, T data) {
        return new SuccessResponse<>(message, data, Instant.now());
    }
}
