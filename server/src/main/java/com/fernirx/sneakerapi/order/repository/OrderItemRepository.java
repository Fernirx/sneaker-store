package com.fernirx.sneakerapi.order.repository;

import com.fernirx.sneakerapi.order.entity.Order;
import com.fernirx.sneakerapi.order.entity.OrderItem;
import com.fernirx.sneakerapi.order.enums.OrderStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    @Query("SELECT oi FROM OrderItem oi JOIN FETCH oi.variant WHERE oi.order = :order")
    List<OrderItem> findAllByOrder(@Param("order") Order order);

    @Query("SELECT oi.order FROM OrderItem oi " +
            "WHERE oi.order.customer.user.id = :userId AND oi.order.status = :status " +
            "AND oi.variant.product.id = :productId ORDER BY oi.order.createdAt DESC")
    List<Order> findOrdersForProductByStatus(@Param("userId") Long userId, @Param("productId") Long productId,
                                              @Param("status") OrderStatus status, Pageable pageable);

    @Query("SELECT oi.productCode, oi.productName, SUM(oi.quantity) AS totalQty FROM OrderItem oi " +
            "WHERE oi.order.status <> com.fernirx.sneakerapi.order.enums.OrderStatus.CANCELLED " +
            "GROUP BY oi.productCode, oi.productName ORDER BY totalQty DESC")
    List<Object[]> findTopSellingProducts(Pageable pageable);
}
