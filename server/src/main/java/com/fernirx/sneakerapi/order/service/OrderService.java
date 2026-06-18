package com.fernirx.sneakerapi.order.service;

import com.fernirx.sneakerapi.order.dto.request.CreateOrderRequest;
import com.fernirx.sneakerapi.order.dto.request.OrderFilterRequest;
import com.fernirx.sneakerapi.order.dto.request.UpdateOrderStatusRequest;
import com.fernirx.sneakerapi.order.dto.response.OrderInternalResponse;
import com.fernirx.sneakerapi.order.dto.response.OrderResponse;
import com.fernirx.sneakerapi.order.dto.response.OrderStatusHistoryResponse;
import com.fernirx.sneakerapi.order.entity.Order;
import com.fernirx.sneakerapi.order.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface OrderService {

    // Guest OTP
    void sendGuestOtp(String email);

    // Customer/guest facing
    OrderResponse createOrder(Long userId, String guestToken, CreateOrderRequest request);
    Page<OrderResponse> getMyOrders(Long userId, String guestToken, Pageable pageable);
    OrderResponse getMyOrderDetail(Long orderId, Long userId, String guestToken);
    List<OrderStatusHistoryResponse> getMyOrderHistory(Long orderId, Long userId, String guestToken);
    void customerCancelOrder(Long orderId, Long userId, String guestToken);

    // Internal/admin
    Page<OrderInternalResponse> getAll(OrderFilterRequest filter, Pageable pageable);
    OrderInternalResponse getById(Long id);
    List<OrderStatusHistoryResponse> getHistory(Long id);
    OrderInternalResponse updateStatus(Long id, UpdateOrderStatusRequest request, Long changedByUserId);

    // Cross-module (Payment, Scheduler)
    Order findEntityById(Long id);
    void changeStatus(Long orderId, OrderStatus newStatus, Long changedByUserId, String note);
    void cancelOrder(Long orderId, String reason);
    void markAsPaid(Long orderId);
}
