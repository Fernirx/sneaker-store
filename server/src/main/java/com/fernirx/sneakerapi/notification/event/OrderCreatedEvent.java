package com.fernirx.sneakerapi.notification.event;

import java.math.BigDecimal;

/**
 * trackingToken dùng để tra cứu trạng thái đơn hàng một cách an toàn.
 */
public record OrderCreatedEvent(
        Long orderId, String orderCode, String email, String recipientName, BigDecimal totalAmount, String trackingToken
) {}
