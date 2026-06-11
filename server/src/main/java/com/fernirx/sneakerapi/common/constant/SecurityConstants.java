package com.fernirx.sneakerapi.common.constant;

public final class SecurityConstants {
    private SecurityConstants() {
        throw new UnsupportedOperationException("This is a constants class and cannot be instantiated");
    }

    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String BEARER_PREFIX = "Bearer ";
    public static final int BEARER_PREFIX_LENGTH = 7;
    public static final String CONTENT_TYPE_JSON = "application/json";
}
