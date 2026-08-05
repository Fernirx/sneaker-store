package com.fernirx.sneakerapi.order.scheduler;

import com.fernirx.sneakerapi.order.entity.Order;
import com.fernirx.sneakerapi.order.enums.OrderPaymentStatus;
import com.fernirx.sneakerapi.order.enums.OrderStatus;
import com.fernirx.sneakerapi.order.enums.PaymentMethod;
import com.fernirx.sneakerapi.order.repository.OrderRepository;
import com.fernirx.sneakerapi.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class OrderScheduler {
    private final OrderRepository orderRepository;
    private final OrderService orderService;

    // Chạy mỗi phút — hủy đơn VNPAY chưa thanh toán quá hạn (COD không cần thanh toán online nên bỏ qua)
    @Scheduled(cron = "0 * * * * *")
    public void cancelExpiredOrders() {
        List<Order> expired = orderRepository.findByPaymentMethodAndPaymentStatusAndStatusAndExpiredAtBefore(
                PaymentMethod.VNPAY, OrderPaymentStatus.UNPAID, OrderStatus.PENDING, LocalDateTime.now());
        for (Order order : expired) {
            orderService.cancelOrder(order.getId(), "Hết hạn thanh toán");
        }
    }
    // Chạy mỗi 30 phút — đồng bộ trạng thái giao hàng cho các đơn đang SHIPPING từ GHN
    @Scheduled(cron = "0 0/30 * * * *")
    public void syncShippingStatus() {
        List<Order> shippingOrders = orderRepository.findByStatus(OrderStatus.SHIPPING);
        for (Order order : shippingOrders) {
            try {
                orderService.syncShipmentStatus(order.getId(), null);
            } catch (Exception e) {
                // Bỏ qua lỗi của từng đơn để không làm chết cả tiến trình đồng bộ
            }
        }
    }
}
