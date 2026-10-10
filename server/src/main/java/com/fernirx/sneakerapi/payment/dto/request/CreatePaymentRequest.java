package com.fernirx.sneakerapi.payment.dto.request;

import jakarta.validation.constraints.NotNull;

public record CreatePaymentRequest(
        @NotNull(message = "{validation.field.not_blank}")
        Long orderId
) {}
