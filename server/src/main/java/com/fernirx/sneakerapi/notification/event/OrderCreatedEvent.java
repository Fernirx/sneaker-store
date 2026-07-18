package com.fernirx.sneakerapi.notification.event;

import java.math.BigDecimal;

/**
 * guestToken chỉ khác null với đơn khách vãng lai - dùng để build link tra cứu đơn kèm token trong mail
 * xác nhận (OrderMailEventListener), KHÔNG cấp thêm quyền gì ngoài phạm vi guest đã có (xem đường token
 * hiện có ở OrderController: chỉ xem/theo dõi/hủy đơn của chính mình, không áp dụng cho đổi/trả).
 */
public record OrderCreatedEvent(
        Long orderId, String orderCode, String email, String recipientName, BigDecimal totalAmount, String guestToken
) {}
