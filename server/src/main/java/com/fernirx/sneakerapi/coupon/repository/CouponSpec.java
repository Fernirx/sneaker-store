package com.fernirx.sneakerapi.coupon.repository;

import com.fernirx.sneakerapi.coupon.dto.request.CouponFilterRequest;
import com.fernirx.sneakerapi.coupon.entity.Coupon;
import com.fernirx.sneakerapi.coupon.enums.DiscountType;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

public class CouponSpec {

    public static Specification<Coupon> build(CouponFilterRequest filter) {
        return Specification
                .where(hasKeyword(filter.search()))
                .and(isActive(filter.active()))
                .and(hasDiscountType(filter.discountType()));
    }

    private static Specification<Coupon> hasKeyword(String keyword) {
        return (root, query, cb) -> {
            if (!StringUtils.hasText(keyword)) return null;
            String pattern = "%" + keyword.toLowerCase() + "%";
            return cb.like(cb.lower(root.get("code")), pattern);
        };
    }

    private static Specification<Coupon> isActive(Boolean active) {
        return (root, query, cb) -> active == null ? null : cb.equal(root.get("active"), active);
    }

    private static Specification<Coupon> hasDiscountType(DiscountType type) {
        return (root, query, cb) -> type == null ? null : cb.equal(root.get("discountType"), type);
    }
}
