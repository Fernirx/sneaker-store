package com.fernirx.sneakerapi.coupon.service.impl;

import com.fernirx.sneakerapi.common.enums.ErrorCode;
import com.fernirx.sneakerapi.common.exception.BusinessException;
import com.fernirx.sneakerapi.coupon.dto.request.CouponFilterRequest;
import com.fernirx.sneakerapi.coupon.dto.request.CouponPreviewRequest;
import com.fernirx.sneakerapi.coupon.dto.request.CreateCouponRequest;
import com.fernirx.sneakerapi.coupon.dto.request.UpdateCouponRequest;
import com.fernirx.sneakerapi.coupon.dto.response.CouponApplyResult;
import com.fernirx.sneakerapi.coupon.dto.response.CouponInternalResponse;
import com.fernirx.sneakerapi.coupon.dto.response.CouponPreviewResponse;
import com.fernirx.sneakerapi.coupon.entity.Coupon;
import com.fernirx.sneakerapi.coupon.entity.CouponUsage;
import com.fernirx.sneakerapi.coupon.enums.DiscountType;
import com.fernirx.sneakerapi.coupon.mapper.CouponMapper;
import com.fernirx.sneakerapi.coupon.repository.CouponRepository;
import com.fernirx.sneakerapi.coupon.repository.CouponSpec;
import com.fernirx.sneakerapi.coupon.repository.CouponUsageRepository;
import com.fernirx.sneakerapi.coupon.service.CouponService;
import com.fernirx.sneakerapi.order.entity.Order;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@Service
@Transactional
@RequiredArgsConstructor
public class CouponServiceImpl implements CouponService {

    private final CouponRepository couponRepository;
    private final CouponUsageRepository couponUsageRepository;
    private final CouponMapper couponMapper;

    @Override
    @Transactional(readOnly = true)
    public CouponPreviewResponse preview(CouponPreviewRequest request) {
        Coupon coupon = couponRepository.findByCodeIgnoreCase(request.code())
                .orElseThrow(() -> BusinessException.notFound("label.coupon"));
        validate(coupon, request.orderAmount());
        BigDecimal discountAmount = calculateDiscount(coupon, request.orderAmount());
        BigDecimal finalAmount = request.orderAmount().subtract(discountAmount);
        return new CouponPreviewResponse(
                coupon.getCode(),
                coupon.getDiscountType(),
                coupon.getDiscountValue(),
                coupon.getMinOrderAmount(),
                coupon.getMaxDiscountAmount(),
                discountAmount,
                finalAmount
        );
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CouponInternalResponse> getAll(CouponFilterRequest filter, Pageable pageable) {
        return couponRepository.findAll(CouponSpec.build(filter), pageable)
                .map(couponMapper::toInternalResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public CouponInternalResponse getById(Long id) {
        return couponMapper.toInternalResponse(findById(id));
    }

    @Override
    public CouponInternalResponse create(CreateCouponRequest request) {
        if (couponRepository.existsByCodeIgnoreCase(request.code())) {
            throw BusinessException.alreadyExists("label.coupon");
        }
        Coupon coupon = new Coupon();
        coupon.setCode(request.code().toUpperCase());
        coupon.setDescription(request.description());
        coupon.setDiscountType(request.discountType());
        coupon.setDiscountValue(request.discountValue());
        coupon.setMinOrderAmount(request.minOrderAmount());
        coupon.setMaxDiscountAmount(request.maxDiscountAmount());
        coupon.setUsageLimit(request.usageLimit());
        coupon.setUsedCount(0);
        coupon.setUserUsageLimit(request.userUsageLimit());
        coupon.setStartDate(request.startDate());
        coupon.setEndDate(request.endDate());
        coupon.setActive(true);
        return couponMapper.toInternalResponse(couponRepository.save(coupon));
    }

    @Override
    public CouponInternalResponse update(Long id, UpdateCouponRequest request) {
        Coupon coupon = findById(id);
        couponMapper.updateCoupon(request, coupon);
        return couponMapper.toInternalResponse(couponRepository.save(coupon));
    }

    @Override
    public void delete(Long id) {
        couponRepository.delete(findById(id));
    }

    @Override
    @Transactional(readOnly = true)
    public CouponApplyResult validate(String code, BigDecimal orderAmount, String email, String phone) {
        Coupon coupon = couponRepository.findByCodeIgnoreCase(code)
                .orElseThrow(() -> BusinessException.notFound("label.coupon"));
        validate(coupon, orderAmount);
        validateUserUsageLimit(coupon, email, phone);
        BigDecimal discountAmount = calculateDiscount(coupon, orderAmount);
        return new CouponApplyResult(coupon.getId(), coupon.getCode(), discountAmount);
    }

    @Override
    public void recordUsage(Long couponId, Order order, String email, String phone) {
        Coupon coupon = findById(couponId);
        coupon.setUsedCount(coupon.getUsedCount() + 1);
        couponRepository.save(coupon);

        CouponUsage usage = new CouponUsage();
        usage.setCoupon(coupon);
        usage.setEmail(email);
        usage.setPhone(phone);
        usage.setOrder(order);
        couponUsageRepository.save(usage);
    }

    @Override
    public void releaseUsage(Long orderId) {
        couponUsageRepository.findByOrder_Id(orderId).ifPresent(usage -> {
            Coupon coupon = usage.getCoupon();
            coupon.setUsedCount(Math.max(0, coupon.getUsedCount() - 1));
            couponRepository.save(coupon);
            couponUsageRepository.delete(usage);
        });
    }

    private void validateUserUsageLimit(Coupon coupon, String email, String phone) {
        if (coupon.getUserUsageLimit() == null) {
            return;
        }
        long usedByEmail = email != null
                ? couponUsageRepository.countByCoupon_IdAndEmailIgnoreCase(coupon.getId(), email) : 0;
        long usedByPhone = phone != null
                ? couponUsageRepository.countByCoupon_IdAndPhone(coupon.getId(), phone) : 0;
        if (Math.max(usedByEmail, usedByPhone) >= coupon.getUserUsageLimit()) {
            throw BusinessException.of(ErrorCode.COUPON_USAGE_LIMIT);
        }
    }

    private void validate(Coupon coupon, BigDecimal orderAmount) {
        if (!coupon.getActive()) {
            throw BusinessException.bad("label.coupon");
        }
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(coupon.getStartDate())) {
            throw BusinessException.bad("label.coupon");
        }
        if (now.isAfter(coupon.getEndDate())) {
            throw BusinessException.expired("label.coupon");
        }
        if (coupon.getUsageLimit() != null && coupon.getUsedCount() >= coupon.getUsageLimit()) {
            throw BusinessException.of(ErrorCode.COUPON_EXHAUSTED);
        }
        if (coupon.getMinOrderAmount() != null && orderAmount.compareTo(coupon.getMinOrderAmount()) < 0) {
            throw BusinessException.of(ErrorCode.COUPON_MIN_AMOUNT, coupon.getMinOrderAmount());
        }
    }

    private BigDecimal calculateDiscount(Coupon coupon, BigDecimal orderAmount) {
        return switch (coupon.getDiscountType()) {
            case PERCENTAGE -> {
                BigDecimal discount = orderAmount
                        .multiply(coupon.getDiscountValue())
                        .divide(BigDecimal.valueOf(100), 2, RoundingMode.DOWN);
                if (coupon.getMaxDiscountAmount() != null) {
                    discount = discount.min(coupon.getMaxDiscountAmount());
                }
                yield discount;
            }
            case FIXED_AMOUNT -> coupon.getDiscountValue().min(orderAmount);
        };
    }

    private Coupon findById(Long id) {
        return couponRepository.findById(id)
                .orElseThrow(() -> BusinessException.notFound("label.coupon"));
    }
}
