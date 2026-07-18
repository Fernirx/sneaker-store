package com.fernirx.sneakerapi.order.repository;

import com.fernirx.sneakerapi.order.entity.Order;
import com.fernirx.sneakerapi.order.entity.OrderStatusHistory;
import com.fernirx.sneakerapi.order.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrderStatusHistoryRepository extends JpaRepository<OrderStatusHistory, Long> {
    List<OrderStatusHistory> findByOrderOrderByCreatedAtAsc(Order order);

    Optional<OrderStatusHistory> findTopByOrder_IdAndNewStatusOrderByCreatedAtDesc(Long orderId, OrderStatus newStatus);
}
