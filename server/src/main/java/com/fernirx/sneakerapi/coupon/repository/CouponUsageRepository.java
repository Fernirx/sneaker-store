package com.fernirx.sneakerapi.coupon.repository;

import com.fernirx.sneakerapi.coupon.entity.CouponUsage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CouponUsageRepository extends JpaRepository<CouponUsage, Long> {
    long countByCoupon_IdAndEmailIgnoreCase(Long couponId, String email);
    long countByCoupon_IdAndPhone(Long couponId, String phone);
    Optional<CouponUsage> findByOrder_Id(Long orderId);
    void deleteByOrder_Id(Long orderId);
}
