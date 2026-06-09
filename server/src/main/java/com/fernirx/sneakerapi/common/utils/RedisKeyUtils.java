package com.fernirx.sneakerapi.common.utils;

import lombok.experimental.UtilityClass;

@UtilityClass
public class RedisKeyUtils {
    public static final String AUTH_ACCESS_REVOKED_PREFIX = "auth:access:revoked:";
    public static final String AUTH_REFRESH_REVOKED_PREFIX = "auth:refresh:revoked:";

    private static final String OTP_KEY_PREFIX = "otp:%s:%s";
    private static final String OTP_ATTEMPTS_KEY_PREFIX = "otp:%s:attempts:%s";
    private static final String OTP_COOLDOWN_KEY_PREFIX = "otp:%s:cooldown:%s";

    public String revokedAccessKey(String jti) {
        return AUTH_ACCESS_REVOKED_PREFIX + jti;
    }

    public String revokedRefreshKey(String jti) {
        return AUTH_REFRESH_REVOKED_PREFIX + jti;
    }

    public String otpKey(String purpose, String email) {
        return String.format(OTP_KEY_PREFIX, purpose, email);
    }

    public String otpAttemptsKey(String purpose, String email) {
        return String.format(OTP_ATTEMPTS_KEY_PREFIX, purpose, email);
    }

    public String otpCooldownKey(String purpose, String email) {
        return String.format(OTP_COOLDOWN_KEY_PREFIX, purpose, email);
    }
}
