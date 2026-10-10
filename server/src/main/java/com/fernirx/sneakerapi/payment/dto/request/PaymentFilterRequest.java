package com.fernirx.sneakerapi.payment.dto.request;

import com.fernirx.sneakerapi.payment.enums.PaymentStatus;

public record PaymentFilterRequest(
        Long orderId,
        PaymentStatus status
) {}
