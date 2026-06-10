package com.fernirx.sneakerapi.security.service;

public interface TokenBlacklistService {
    void blacklistAccessToken(String token);
    void blacklistRefreshToken(String token);
    boolean isAccessTokenBlacklisted(String token);
    boolean isRefreshTokenBlacklisted(String token);
}