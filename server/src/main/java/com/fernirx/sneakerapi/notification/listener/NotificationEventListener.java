package com.fernirx.sneakerapi.notification.listener;

import com.fernirx.sneakerapi.common.enums.Role;
import com.fernirx.sneakerapi.notification.dto.command.CreateNotificationCommand;
import com.fernirx.sneakerapi.notification.enums.NotificationType;
import com.fernirx.sneakerapi.notification.event.CouponCreatedEvent;
import com.fernirx.sneakerapi.notification.event.LowStockEvent;
import com.fernirx.sneakerapi.notification.event.OrderCancelledEvent;
import com.fernirx.sneakerapi.notification.event.OrderCreatedEvent;
import com.fernirx.sneakerapi.notification.event.OutOfStockEvent;
import com.fernirx.sneakerapi.notification.event.ProductOnSaleEvent;
import com.fernirx.sneakerapi.notification.event.ProductPublishedEvent;
import com.fernirx.sneakerapi.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Lắng nghe sau khi transaction gốc COMMIT thành công (AFTER_COMMIT) rồi mới tạo + đẩy thông báo -
 * tránh việc push SSE cho 1 nghiệp vụ (đơn hàng, tồn kho...) mà sau đó lại rollback (xem Context ở plan).
 * Chạy @Async (thread riêng, không chặn request gốc) - lần đầu dùng ApplicationEventPublisher trong codebase,
 * cố tình giới hạn phạm vi chỉ cho notification, không áp dụng lại pattern gọi trực tiếp Service ở các module khác.
 */
@Component
@RequiredArgsConstructor
public class NotificationEventListener {
    private final NotificationService notificationService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onOrderCreated(OrderCreatedEvent event) {
        String title = "Đơn hàng mới #" + event.orderCode();
        String message = "Đơn hàng #" + event.orderCode() + " vừa được tạo, cần xác nhận.";
        String link = "/admin/orders/" + event.orderId();
        notifyRole(NotificationType.ORDER, Role.ROLE_SALE, title, message, link);
        notifyRole(NotificationType.ORDER, Role.ROLE_WAREHOUSE, title, message, link);
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onOrderCancelled(OrderCancelledEvent event) {
        String title = "Đơn hàng bị hủy #" + event.orderCode();
        String message = "Đơn hàng #" + event.orderCode() + " đã bị hủy. Lý do: " + event.reason();
        String link = "/admin/orders/" + event.orderId();
        notifyRole(NotificationType.ORDER, Role.ROLE_SALE, title, message, link);
        notifyRole(NotificationType.ORDER, Role.ROLE_WAREHOUSE, title, message, link);
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onLowStock(LowStockEvent event) {
        String title = "Sắp hết hàng: " + event.productName();
        String message = "SKU " + event.sku() + " chỉ còn " + event.newStock()
                + " sản phẩm (ngưỡng cảnh báo " + event.minStockLevel() + ").";
        String link = "/admin/products?search=" + event.sku();
        notifyRole(NotificationType.INVENTORY, Role.ROLE_WAREHOUSE, title, message, link);
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onOutOfStock(OutOfStockEvent event) {
        String title = "Hết hàng: " + event.productName();
        String message = "SKU " + event.sku() + " đã hết hàng trong kho.";
        String link = "/admin/products?search=" + event.sku();
        notifyRole(NotificationType.INVENTORY, Role.ROLE_WAREHOUSE, title, message, link);
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onCouponCreated(CouponCreatedEvent event) {
        String title = "Mã giảm giá mới: " + event.code();
        String message = "Nhập mã " + event.code() + " để nhận ưu đãi ngay hôm nay!";
        notifyAllCustomers(NotificationType.PROMOTION, title, message, null);
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onProductPublished(ProductPublishedEvent event) {
        String title = "Sản phẩm mới: " + event.productName();
        String message = event.productName() + " vừa lên kệ, khám phá ngay!";
        notifyAllCustomers(NotificationType.PRODUCT, title, message, "/products/" + event.slug());
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onProductOnSale(ProductOnSaleEvent event) {
        String title = "Giảm giá: " + event.productName();
        String message = event.productName() + " đang được giảm giá, mua ngay kẻo lỡ!";
        notifyAllCustomers(NotificationType.PRODUCT, title, message, "/products/" + event.slug());
    }

    private void notifyRole(NotificationType type, Role role, String title, String message, String link) {
        notificationService.create(CreateNotificationCommand.toRole(type, role, title, message, link));
    }

    private void notifyAllCustomers(NotificationType type, String title, String message, String link) {
        notificationService.create(CreateNotificationCommand.toAllCustomers(type, title, message, link));
    }
}
