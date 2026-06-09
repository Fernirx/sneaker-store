package com.fernirx.sneakerapi.auth.config;

import jakarta.validation.constraints.Min;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Data
@Validated
@ConfigurationProperties(prefix = "application.otp")
public class OTPProperties {
    /** Thời gian hết hạn OTP (phút) */
    @Min(1)
    private int ttl = 5;

    /** Số lần nhập sai tối đa */
    @Min(1)
    private int maxAttempts = 5;

    /** Thời gian chờ gửi lại OTP (giây) */
    @Min(0)
    private int resendCooldown = 60;
}
