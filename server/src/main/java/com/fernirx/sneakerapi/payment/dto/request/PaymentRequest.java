package com.fernirx.sneakerapi.payment.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record PaymentRequest(
        @NotNull(message = "{validation.field.not_blank}")
        Long orderId,

        @NotBlank(message = "{validation.field.not_blank}")
        String orderCode,

        @NotNull(message = "{validation.field.not_blank}")
        @Min(value = 1000, message = "{validation.number.min}")
        BigDecimal amount
) {}
