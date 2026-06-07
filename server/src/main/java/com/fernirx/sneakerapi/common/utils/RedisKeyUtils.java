package com.fernirx.sneakerapi.common.utils;

import lombok.experimental.UtilityClass;

@UtilityClass
public class RedisKeyUtils {
    public static final String AUTH_ACCESS_REVOKED_PREFIX = "auth:access:revoked:";
    public static final String AUTH_REFRESH_REVOKED_PREFIX = "auth:refresh:revoked:";

    public String revokedAccessKey(String jti) {
        return AUTH_ACCESS_REVOKED_PREFIX + jti;
    }

    public String revokedRefreshKey(String jti) {
        return AUTH_REFRESH_REVOKED_PREFIX + jti;
    }
}
