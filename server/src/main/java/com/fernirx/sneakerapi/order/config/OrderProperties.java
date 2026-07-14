package com.fernirx.sneakerapi.order.config;

import jakarta.validation.constraints.Positive;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Data
@Validated
@ConfigurationProperties(prefix = "order")
public class OrderProperties {
    /** Thời gian tối đa để thanh toán trước khi đơn tự hủy (phút) */
    @Positive
    private int expireMinutes = 10;

    /** Số lần thanh toán thất bại tối đa trước khi đơn tự hủy */
    @Positive
    private int maxPaymentAttempts = 4;
}
