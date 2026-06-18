package com.fernirx.sneakerapi.payment.dto.response;

import com.fernirx.sneakerapi.payment.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PaymentInternalResponse(
        Long id,
        Long orderId,
        String orderCode,
        BigDecimal amount,
        PaymentStatus status,
        String transactionId,
        String responseCode,
        LocalDateTime createdAt
) {}
