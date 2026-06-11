package com.fernirx.sneakerapi.common.exception;

import com.fernirx.sneakerapi.common.enums.ErrorCode;
import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {
    private final ErrorCode errorCode;
    private final Object[] args;

    public BusinessException(ErrorCode errorCode, Object... args) {
        super(errorCode.getMessageKey());
        this.errorCode = errorCode;
        this.args = args;
    }

    public static BusinessException of(ErrorCode errorCode, Object... args) {
        return new BusinessException(errorCode, args);
    }

    public static BusinessException bad(Object... args) {
        return new BusinessException(ErrorCode.INVALID_DATA, args);
    }

    public static BusinessException expired(Object... args) {
        return new BusinessException(ErrorCode.DATA_EXPIRED, args);
    }

    public static BusinessException notFound(Object... args) {
        return new BusinessException(ErrorCode.NOT_FOUND, args);
    }

    public static BusinessException alreadyExists(Object... args) {
        return new BusinessException(ErrorCode.ALREADY_EXISTS, args);
    }

    public static BusinessException inUse(Object... args) {
        return new BusinessException(ErrorCode.IN_USE, args);
    }

    public static BusinessException tooMany(Object... args) {
        return new BusinessException(ErrorCode.LIMIT_EXCEEDED, args);
    }

    public static BusinessException cooldown(long seconds) {
        return new BusinessException(ErrorCode.COOLDOWN_ACTIVE, seconds);
    }
}
