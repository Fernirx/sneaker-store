package com.fernirx.sneakerapi.order.config;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;

@Data
@Validated
@ConfigurationProperties(prefix = "order")
public class OrderProperties {
    /** Thời gian tối đa để thanh toán trước khi đơn tự hủy (phút) */
    @Positive
    private int expireMinutes = 15;

    /** Số lần thanh toán thất bại tối đa trước khi đơn tự hủy */
    @Positive
    private int maxPaymentAttempts = 4;

    /** Phí ship tạm — flat fee, TODO: tính theo GHN khi tích hợp giao hàng nhanh */
    @PositiveOrZero
    private BigDecimal shippingFee = BigDecimal.valueOf(30000);
}
