package com.fernirx.sneakerapi.notification.listener;

import com.fernirx.sneakerapi.notification.event.OrderCreatedEvent;
import com.fernirx.sneakerapi.notification.service.MailService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.util.StringUtils;

/**
 * Gửi mail xác nhận đơn hàng - tách riêng khỏi NotificationEventListener (khác dependency: MailService
 * thay vì NotificationService) nhưng dùng chung OrderCreatedEvent, cùng nguyên tắc AFTER_COMMIT + @Async
 * đã thống nhất cho mọi side-effect không thể rollback trong module này.
 */
@Component
@RequiredArgsConstructor
public class OrderMailEventListener {
    private final MailService mailService;

    @Value("${application.frontend-url}")
    private String frontendUrl;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onOrderCreated(OrderCreatedEvent event) {
        if (!StringUtils.hasText(event.email())) {
            return;
        }
        String orderUrl = frontendUrl + "/orders/" + event.orderId()
                + (StringUtils.hasText(event.guestToken())
                        ? "?orderToken=" + event.guestToken()
                        : "");
        mailService.sendOrderConfirmation(event.email(), event.recipientName(), event.orderCode(), event.totalAmount(), orderUrl);
    }
}
