package com.fernirx.sneakerapi.coupon.repository;

import com.fernirx.sneakerapi.coupon.entity.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CouponRepository extends JpaRepository<Coupon, Long>, JpaSpecificationExecutor<Coupon> {
    Optional<Coupon> findByCodeIgnoreCase(String code);
    boolean existsByCodeIgnoreCase(String code);

    // Atomic, điều kiện usageLimit ngay trong WHERE — tránh lost-update khi 2 đơn dùng cùng coupon đồng thời
    @Modifying
    @Query("UPDATE Coupon c SET c.usedCount = c.usedCount + 1 " +
            "WHERE c.id = :id AND (c.usageLimit IS NULL OR c.usedCount < c.usageLimit)")
    int incrementUsedCount(@Param("id") Long id);

    @Modifying
    @Query("UPDATE Coupon c SET c.usedCount = c.usedCount - 1 WHERE c.id = :id AND c.usedCount > 0")
    int decrementUsedCount(@Param("id") Long id);
}
