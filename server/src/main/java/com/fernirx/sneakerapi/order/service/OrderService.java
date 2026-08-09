package com.fernirx.sneakerapi.order.service;

import com.fernirx.sneakerapi.order.dto.request.CreateOrderRequest;
import com.fernirx.sneakerapi.order.dto.request.OrderFilterRequest;
import com.fernirx.sneakerapi.order.dto.request.UpdateOrderStatusRequest;
import com.fernirx.sneakerapi.order.dto.response.OrderInternalResponse;
import com.fernirx.sneakerapi.order.dto.response.OrderResponse;
import com.fernirx.sneakerapi.order.dto.response.OrderStatusHistoryResponse;
import com.fernirx.sneakerapi.order.entity.Order;
import com.fernirx.sneakerapi.order.entity.OrderItem;
import com.fernirx.sneakerapi.order.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface OrderService {

    // Customer/guest facing
    OrderResponse createOrder(Long userId, String guestToken, String idempotencyKey, CreateOrderRequest request);
    Page<OrderResponse> getMyOrders(Long userId, String guestToken, Pageable pageable);
    OrderResponse getMyOrderDetail(Long orderId, Long userId, String guestToken);
    OrderResponse trackOrder(String trackingToken);
    List<OrderStatusHistoryResponse> trackOrderHistory(String trackingToken);
    List<OrderStatusHistoryResponse> getMyOrderHistory(Long orderId, Long userId, String guestToken);
    void customerCancelOrder(Long orderId, Long userId, String guestToken);

    // Internal/admin
    Page<OrderInternalResponse> getAll(OrderFilterRequest filter, Pageable pageable);
    OrderInternalResponse getById(Long id);
    List<OrderStatusHistoryResponse> getHistory(Long id);
    OrderInternalResponse updateStatus(Long id, UpdateOrderStatusRequest request, Long changedByUserId, Collection<String> callerRoles);
    OrderInternalResponse createShipment(Long orderId, Long changedByUserId);
    OrderInternalResponse cancelShipment(Long orderId, Long changedByUserId);
    OrderInternalResponse syncShipmentStatus(Long orderId, Long changedByUserId);

    // Cross-module (Payment, Scheduler, Review)
    Order findEntityById(Long id);
    Order findEntityByIdForUpdate(Long id);
    Order findOwnedEntityById(Long orderId, Long userId, String guestToken);
    void changeStatus(Long orderId, OrderStatus newStatus, Long changedByUserId, String note);
    void cancelOrder(Long orderId, String reason);
    void markAsPaid(Long orderId);
    void flagLatePayment(Long orderId, String note);
    Optional<Order> findDeliveredOrderForProduct(Long userId, Long productId);

    // Cross-module (Returns) - đóng gói truy cập order_items/order_status_history, tránh module khác
    // tiêm thẳng repository của module order (vi phạm ranh giới module).
    List<OrderItem> findItemsByOrder(Order order);
    OrderItem findItemById(Long orderItemId);
    Optional<LocalDateTime> findDeliveredAt(Long orderId);
}
