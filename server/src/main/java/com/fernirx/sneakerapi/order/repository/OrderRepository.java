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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long>, JpaSpecificationExecutor<Order> {

    Page<Order> findByCustomer_User_Id(Long userId, Pageable pageable);

    Page<Order> findByGuestToken(String guestToken, Pageable pageable);

    Optional<Order> findByIdAndCustomer_User_Id(Long id, Long userId);

    Optional<Order> findByIdAndGuestToken(Long id, String guestToken);

    List<Order> findByPaymentMethodAndPaymentStatusAndStatusAndExpiredAtBefore(
            PaymentMethod paymentMethod, OrderPaymentStatus paymentStatus, OrderStatus status, LocalDateTime expiredBefore);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT o FROM Order o WHERE o.id = :id")
    Optional<Order> findByIdForUpdate(@Param("id") Long id);
}