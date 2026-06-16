package com.fernirx.sneakerapi.coupon.service;

import com.fernirx.sneakerapi.coupon.dto.request.CouponFilterRequest;
import com.fernirx.sneakerapi.coupon.dto.request.CouponPreviewRequest;
import com.fernirx.sneakerapi.coupon.dto.request.CreateCouponRequest;
import com.fernirx.sneakerapi.coupon.dto.request.UpdateCouponRequest;
import com.fernirx.sneakerapi.coupon.dto.response.CouponInternalResponse;
import com.fernirx.sneakerapi.coupon.dto.response.CouponPreviewResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CouponService {
    CouponPreviewResponse preview(CouponPreviewRequest request);
    Page<CouponInternalResponse> getAll(CouponFilterRequest filter, Pageable pageable);
    CouponInternalResponse getById(Long id);
    CouponInternalResponse create(CreateCouponRequest request);
    CouponInternalResponse update(Long id, UpdateCouponRequest request);
    void delete(Long id);
}
