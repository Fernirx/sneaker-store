package com.fernirx.sneakerapi.payment.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Data
@Validated
@ConfigurationProperties(prefix = "vnpay")
public class VNPayProperties {
    /** Mã website (Terminal ID) do VNPay cấp */
    @NotBlank
    private String tmnCode;

    /** Chuỗi bí mật dùng để ký và xác thực checksum */
    @NotBlank
    private String hashSecret;

    /** URL cổng thanh toán VNPay (sandbox vs production) */
    @NotBlank
    private String paymentUrl;

    /** URL VNPay redirect về sau khi thanh toán xong */
    @NotBlank
    private String returnUrl;

    /** Thời gian hết hạn của link thanh toán (phút) */
    @Positive
    private int expireMinutes;
}