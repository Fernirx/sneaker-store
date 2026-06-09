package com.fernirx.sneakerapi.auth.service.impl;

import com.fernirx.sneakerapi.auth.config.OTPProperties;
import com.fernirx.sneakerapi.auth.service.OtpService;
import com.fernirx.sneakerapi.common.exception.BusinessException;
import com.fernirx.sneakerapi.common.utils.RedisKeyUtils;
import com.fernirx.sneakerapi.notification.service.MailService;
import com.fernirx.sneakerapi.user.enums.OtpPurpose;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;

@Service
@RequiredArgsConstructor
public class OtpServiceImpl implements OtpService {
    private final StringRedisTemplate stringRedisTemplate;
    private final OTPProperties otpProperties;
    private final PasswordEncoder passwordEncoder;
    private final MailService mailService;

    private static final int OTP_BOUND = 1_000_000;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    @Override
    public void sendOtp(String email, String name, OtpPurpose purpose) {
        String p = purpose.name().toLowerCase();

        if (Boolean.TRUE.equals(stringRedisTemplate.hasKey(RedisKeyUtils.otpCooldownKey(p, email)))) {
            throw BusinessException.cooldown(otpProperties.getResendCooldown());
        }

        String rawOtp = generateOtp();

        stringRedisTemplate.opsForValue().set(
                RedisKeyUtils.otpKey(p, email),
                passwordEncoder.encode(rawOtp),
                Duration.ofMinutes(otpProperties.getTtl())
        );
        stringRedisTemplate.delete(RedisKeyUtils.otpAttemptsKey(p, email));
        stringRedisTemplate.opsForValue().set(
                RedisKeyUtils.otpCooldownKey(p, email),
                "1",
                Duration.ofSeconds(otpProperties.getResendCooldown())
        );

        String displayName = (name != null && !name.isBlank()) ? name : email.split("@")[0];
        mailService.sendVerifyEmailOtp(email, displayName, rawOtp, otpProperties.getTtl());
    }

    @Override
    public void verifyOtp(String email, String rawOtp, OtpPurpose purpose) {
        String p = purpose.name().toLowerCase();
        String otpKey = RedisKeyUtils.otpKey(p, email);
        String attemptsKey = RedisKeyUtils.otpAttemptsKey(p, email);

        String hashedOtp = stringRedisTemplate.opsForValue().get(otpKey);
        if (hashedOtp == null) {
            throw BusinessException.expired("label.otp");
        }

        String attemptsStr = stringRedisTemplate.opsForValue().get(attemptsKey);
        int attempts = attemptsStr != null ? Integer.parseInt(attemptsStr) : 0;
        if (attempts >= otpProperties.getMaxAttempts()) {
            throw BusinessException.tooMany("label.otp");
        }

        if (!passwordEncoder.matches(rawOtp, hashedOtp)) {
            Long newAttempts = stringRedisTemplate.opsForValue().increment(attemptsKey);
            if (newAttempts != null && newAttempts == 1L) {
                stringRedisTemplate.expire(attemptsKey, Duration.ofMinutes(otpProperties.getTtl()));
            }
            throw BusinessException.bad("label.otp");
        }

        stringRedisTemplate.delete(otpKey);
        stringRedisTemplate.delete(attemptsKey);
    }

    private String generateOtp() {
        return String.format("%06d", SECURE_RANDOM.nextInt(OTP_BOUND));
    }
}