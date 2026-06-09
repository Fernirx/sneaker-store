package com.fernirx.sneakerapi.security.service.impl;

import com.fernirx.sneakerapi.common.utils.RedisKeyUtils;
import com.fernirx.sneakerapi.security.jwt.JwtProvider;
import com.fernirx.sneakerapi.security.service.TokenBlacklistService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class TokenBlacklistServiceImpl implements TokenBlacklistService {
    private final StringRedisTemplate redisTemplate;
    private final JwtProvider jwtProvider;

    @Override
    public void blacklistAccessToken(String token) {
        blacklist(token, RedisKeyUtils::revokedAccessKey);
    }

    @Override
    public void blacklistRefreshToken(String token) {
        blacklist(token, RedisKeyUtils::revokedRefreshKey);
    }

    @Override
    public boolean isAccessTokenBlacklisted(String token) {
        return isBlacklisted(token, RedisKeyUtils::revokedAccessKey);
    }

    @Override
    public boolean isRefreshTokenBlacklisted(String token) {
        return isBlacklisted(token, RedisKeyUtils::revokedRefreshKey);
    }

    private void blacklist(String token, Function<String, String> keyFn) {
        String jti = jwtProvider.extractJti(token);
        LocalDateTime expiration = jwtProvider.extractExpiration(token);
        long ttlSeconds = Duration.between(LocalDateTime.now(), expiration).toSeconds();
        if (ttlSeconds > 0) {
            redisTemplate.opsForValue().set(keyFn.apply(jti), "1", Duration.ofSeconds(ttlSeconds));
        }
    }

    private boolean isBlacklisted(String token, Function<String, String> keyFn) {
        String jti = jwtProvider.extractJti(token);
        return Boolean.TRUE.equals(redisTemplate.hasKey(keyFn.apply(jti)));
    }
}