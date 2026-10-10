package com.fernirx.sneakerapi.order.dto.response;

import com.fernirx.sneakerapi.order.enums.OrderStatus;

import java.time.LocalDateTime;

public record OrderStatusHistoryResponse(
        Long id,
        OrderStatus oldStatus,
        OrderStatus newStatus,
        String note,
        LocalDateTime createdAt
) {}
