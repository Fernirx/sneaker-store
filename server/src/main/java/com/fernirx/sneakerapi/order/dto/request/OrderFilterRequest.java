package com.fernirx.sneakerapi.order.dto.request;

import com.fernirx.sneakerapi.order.enums.OrderPaymentStatus;
import com.fernirx.sneakerapi.order.enums.OrderStatus;
import com.fernirx.sneakerapi.order.enums.PaymentMethod;

import java.time.LocalDateTime;

public record OrderFilterRequest(
        String search,
        OrderStatus status,
        OrderPaymentStatus paymentStatus,
        PaymentMethod paymentMethod,
        LocalDateTime fromDate,
        LocalDateTime toDate
) {}
