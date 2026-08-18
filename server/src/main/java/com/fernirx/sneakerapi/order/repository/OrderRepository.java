package com.fernirx.sneakerapi.order.repository;

import com.fernirx.sneakerapi.order.entity.Order;
import com.fernirx.sneakerapi.order.enums.OrderPaymentStatus;
import com.fernirx.sneakerapi.order.enums.OrderStatus;
import com.fernirx.sneakerapi.order.enums.PaymentMethod;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long>, JpaSpecificationExecutor<Order> {

    Page<Order> findByCustomer_User_Id(Long userId, Pageable pageable);

    Page<Order> findByGuestToken(String guestToken, Pageable pageable);

    Optional<Order> findByIdAndCustomer_User_Id(Long id, Long userId);

    Optional<Order> findByIdAndGuestToken(Long id, String guestToken);

    Optional<Order> findByIdempotencyKey(String idempotencyKey);

    Optional<Order> findByTrackingToken(String trackingToken);

    List<Order> findByStatus(OrderStatus status);

    List<Order> findByPaymentMethodAndPaymentStatusAndStatusAndExpiredAtBefore(
            PaymentMethod paymentMethod, OrderPaymentStatus paymentStatus, OrderStatus status, LocalDateTime expiredBefore);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT o FROM Order o WHERE o.id = :id")
    Optional<Order> findByIdForUpdate(@Param("id") Long id);

    @Query("SELECT o.status, COUNT(o) FROM Order o GROUP BY o.status")
    List<Object[]> countGroupByStatus();

    @Query(value = "SELECT SUM(o.total_amount - COALESCE(r.total_refund, 0)) " +
            "FROM orders o " +
            "LEFT JOIN (SELECT order_id, SUM(refund_amount) AS total_refund FROM return_requests WHERE status = 'COMPLETED' GROUP BY order_id) r ON o.id = r.order_id " +
            "WHERE o.payment_status = 'PAID' AND o.created_at >= :from", nativeQuery = true)
    BigDecimal sumRevenueSince(@Param("from") LocalDateTime from);

    @Query(value = "SELECT DATE(o.created_at) AS d, SUM(o.total_amount - COALESCE(r.total_refund, 0)) AS revenue " +
            "FROM orders o " +
            "LEFT JOIN (SELECT order_id, SUM(refund_amount) AS total_refund FROM return_requests WHERE status = 'COMPLETED' GROUP BY order_id) r ON o.id = r.order_id " +
            "WHERE o.payment_status = 'PAID' AND o.created_at >= :from " +
            "GROUP BY DATE(o.created_at) ORDER BY d", nativeQuery = true)
    List<Object[]> findDailyRevenueSince(@Param("from") LocalDateTime from);
}
