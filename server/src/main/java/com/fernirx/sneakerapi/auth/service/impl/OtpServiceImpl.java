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

    /**
     * Sinh và gửi mã OTP qua email theo mục đích sử dụng.
     * Luồng xử lý:
     * 1. Kiểm tra Cooldown: Nếu có key cooldown trong Redis, từ chối gửi để 
     *    chống spam (đợi hết thời gian).
     * 2. Sinh mã OTP ngẫu nhiên 6 chữ số.
     * 3. Mã hóa OTP bằng PasswordEncoder (BCrypt) rồi lưu vào Redis (giúp 
     *    bảo vệ OTP không bị đọc lén từ DB).
     * 4. Xóa số lần thử sai trước đó và thiết lập lại thời gian Cooldown.
     * 5. Gửi email qua MailService với template tương ứng mục đích (Đăng ký, 
     *    Quên MK, Xác thực khách).
     */
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
        switch (purpose) {
            case REGISTER -> mailService.sendVerifyEmailOtp(
                    email, displayName, rawOtp, otpProperties.getTtl());
            case FORGOT_PASSWORD -> mailService.sendForgotPasswordOtp(
                    email, displayName, rawOtp, otpProperties.getTtl());
            case GUEST_ORDER -> mailService.sendOrderVerificationOtp(
                    email, displayName, rawOtp, otpProperties.getTtl());
        }
    }

    /**
     * Xác minh tính hợp lệ của mã OTP.
     * Luồng xử lý:
     * 1. Kiểm tra xem mã OTP còn tồn tại trong Redis không (hết hạn sẽ ném lỗi).
     * 2. Kiểm tra số lần thử nghiệm (attempts). Nếu vượt quá số lần cho phép 
     *    (Max Attempts) thì ném lỗi.
     * 3. Dùng PasswordEncoder để so khớp mã nhập vào với mã đã hash trong Redis.
     *    - Nếu sai: Tăng biến đếm số lần sai. Ném lỗi sai OTP.
     *    - Nếu đúng: Xóa mã OTP và bộ đếm khỏi Redis (nhưng không xóa Cooldown 
     *      để tránh lạm dụng request API).
     */
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

        // Giữ lại OTP cho Guest Order để tránh lỗi race condition (do React strict mode/double click)
        // và giúp khách có thể đặt nhiều đơn liên tiếp trong khoảng thời gian hiệu lực mà không cần lấy OTP mới.
        if (purpose != OtpPurpose.GUEST_ORDER) {
            stringRedisTemplate.delete(otpKey);
            stringRedisTemplate.delete(attemptsKey);
        }
    }

    /**
     * Hàm phụ trợ sinh chuỗi 6 chữ số ngẫu nhiên an toàn bằng SecureRandom.
     */
    private String generateOtp() {
        return String.format("%06d", SECURE_RANDOM.nextInt(OTP_BOUND));
    }
}