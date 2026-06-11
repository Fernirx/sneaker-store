package com.fernirx.sneakerapi.common.exception;

import com.fernirx.sneakerapi.common.enums.ErrorCode;
import lombok.Getter;

@Getter
public class SecurityCustomException extends RuntimeException {
    private final ErrorCode errorCode;
    private final Object[] args;

    public SecurityCustomException(ErrorCode errorCode, Object... args) {
        super(errorCode.getMessageKey());
        this.errorCode = errorCode;
        this.args = args;
    }

    public static SecurityCustomException of(ErrorCode errorCode, Object... args) {
        return new SecurityCustomException(errorCode, args);
    }

    public static SecurityCustomException invalid(Object... args) {
        return new SecurityCustomException(ErrorCode.UNAUTHORIZED, args);
    }

    public static SecurityCustomException forbidden() {
        return new SecurityCustomException(ErrorCode.ACCESS_DENIED);
    }

    public static SecurityCustomException invalidCredentials() {
        return new SecurityCustomException(ErrorCode.INVALID_CREDENTIALS);
    }

    public static SecurityCustomException accountUnavailable() {
        return new SecurityCustomException(ErrorCode.ACCOUNT_UNAVAILABLE);
    }

    public static SecurityCustomException emailNotVerified() {
        return new SecurityCustomException(ErrorCode.EMAIL_NOT_VERIFIED);
    }
}