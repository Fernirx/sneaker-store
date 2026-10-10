package com.fernirx.sneakerapi.security.service.impl;

import com.fernirx.sneakerapi.common.utils.RedisKeyUtils;
import com.fernirx.sneakerapi.security.jwt.JwtProvider;
import com.fernirx.sneakerapi.security.service.TokenBlacklistService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.function.Function;

@Slf4j
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

    private void blacklist(String token, Function<String, String> keyFn) {
        String jti = jwtProvider.extractJti(token);
        LocalDateTime expiration = jwtProvider.extractExpiration(token);
        long ttlSeconds = Duration.between(LocalDateTime.now(), expiration).toSeconds();
        if (ttlSeconds > 0) {
            redisTemplate.opsForValue().set(keyFn.apply(jti), String.valueOf(System.currentTimeMillis()), Duration.ofSeconds(ttlSeconds));
        }
    }

    private boolean isBlacklisted(String token, Function<String, String> keyFn) {
        String jti = jwtProvider.extractJti(token);
        return Boolean.TRUE.equals(redisTemplate.hasKey(keyFn.apply(jti)));
    }

    @Override
    public boolean isRefreshTokenBlacklistedWithGracePeriod(String token, long gracePeriodMs) {
        String jti = jwtProvider.extractJti(token);
        String val = redisTemplate.opsForValue().get(RedisKeyUtils.revokedRefreshKey(jti));
        if (val == null) {
            return false;
        }
        try {
            long revokedAt = Long.parseLong(val);
            if (System.currentTimeMillis() - revokedAt < gracePeriodMs) {
                return false;
            }
        } catch (NumberFormatException ex) {
            log.warn("Giá trị blacklist refresh token không phải timestamp hợp lệ: {}", val);
        }
        return true;
    }
}