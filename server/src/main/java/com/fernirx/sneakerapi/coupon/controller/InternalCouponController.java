package com.fernirx.sneakerapi.coupon.controller;

import com.fernirx.sneakerapi.common.response.PageResponse;
import com.fernirx.sneakerapi.common.response.SuccessResponse;
import com.fernirx.sneakerapi.common.utils.MessageUtil;
import com.fernirx.sneakerapi.coupon.dto.request.CouponFilterRequest;
import com.fernirx.sneakerapi.coupon.dto.request.CreateCouponRequest;
import com.fernirx.sneakerapi.coupon.dto.request.UpdateCouponRequest;
import com.fernirx.sneakerapi.coupon.dto.response.CouponInternalResponse;
import com.fernirx.sneakerapi.coupon.service.CouponService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/internal/coupons")
@RequiredArgsConstructor
@Tag(name = "Internal Coupon API", description = "Quản lý mã giảm giá (nội bộ)")
public class InternalCouponController {
    private final CouponService couponService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SALE', 'MARKETING')")
    @Operation(summary = "Danh sách mã giảm giá")
    public ResponseEntity<PageResponse<CouponInternalResponse>> getAll(
            @ParameterObject @ModelAttribute CouponFilterRequest filter,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        return ResponseEntity.ok(PageResponse.of(couponService.getAll(filter, pageable)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SALE', 'MARKETING')")
    @Operation(summary = "Chi tiết mã giảm giá")
    public ResponseEntity<SuccessResponse<CouponInternalResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(SuccessResponse.of(couponService.getById(id)));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MARKETING')")
    @Operation(summary = "Tạo mã giảm giá mới")
    public ResponseEntity<SuccessResponse<CouponInternalResponse>> create(
            @Valid @RequestBody CreateCouponRequest request) {
        CouponInternalResponse response = couponService.create(request);
        return ResponseEntity.ok(SuccessResponse.of(
                MessageUtil.getMessage("success.resource.created", MessageUtil.getMessage("label.coupon")),
                response
        ));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MARKETING')")
    @Operation(summary = "Cập nhật mã giảm giá")
    public ResponseEntity<SuccessResponse<CouponInternalResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateCouponRequest request) {
        CouponInternalResponse response = couponService.update(id, request);
        return ResponseEntity.ok(SuccessResponse.of(
                MessageUtil.getMessage("success.resource.updated", MessageUtil.getMessage("label.coupon")),
                response
        ));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MARKETING')")
    @Operation(summary = "Xóa mã giảm giá")
    public ResponseEntity<SuccessResponse<Void>> delete(@PathVariable Long id) {
        couponService.delete(id);
        return ResponseEntity.ok(SuccessResponse.of(
                MessageUtil.getMessage("success.resource.deleted", MessageUtil.getMessage("label.coupon"))
        ));
    }
}
