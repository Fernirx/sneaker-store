package com.fernirx.sneakerapi.common.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    /* ================== SYSTEM ================== */
    INTERNAL_SERVER_ERROR("INTERNAL_SERVER_ERROR", "error.server.internal", HttpStatus.INTERNAL_SERVER_ERROR),
    SERVICE_UNAVAILABLE("SERVICE_UNAVAILABLE", "error.server.unavailable", HttpStatus.SERVICE_UNAVAILABLE),

    /* ================== SECURITY ================== */
    UNAUTHORIZED("UNAUTHORIZED", "error.auth.unauthorized", HttpStatus.UNAUTHORIZED),
    ACCESS_DENIED("ACCESS_DENIED", "error.auth.access_denied", HttpStatus.FORBIDDEN),

    /* ================== BUSINESS CORE ================== */
    INVALID_DATA("INVALID_DATA", "error.bus.invalid", HttpStatus.BAD_REQUEST),
    DATA_EXPIRED("DATA_EXPIRED", "error.bus.expired", HttpStatus.BAD_REQUEST),
    NOT_FOUND("NOT_FOUND", "error.bus.not_found", HttpStatus.NOT_FOUND),
    ALREADY_EXISTS("ALREADY_EXISTS", "error.bus.already_exists", HttpStatus.CONFLICT),
    IN_USE("IN_USE", "error.bus.in_use", HttpStatus.CONFLICT),

    /* ================== RATE LIMIT & SPAM ================== */
    LIMIT_EXCEEDED("LIMIT_EXCEEDED", "error.bus.limit_exceeded", HttpStatus.TOO_MANY_REQUESTS),
    COOLDOWN_ACTIVE("COOLDOWN_ACTIVE", "error.bus.cooldown", HttpStatus.TOO_MANY_REQUESTS);

    private final String code;
    private final String messageKey;
    private final HttpStatus httpStatus;

    @JsonValue
    public String getCode() {
        return code;
    }
}
