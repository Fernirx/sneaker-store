package com.fernirx.sneakerapi.coupon.controller;

import com.fernirx.sneakerapi.coupon.dto.request.CouponPreviewRequest;
import com.fernirx.sneakerapi.coupon.dto.response.CouponPreviewResponse;
import com.fernirx.sneakerapi.coupon.service.CouponService;
import com.fernirx.sneakerapi.common.response.SuccessResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/coupons")
@RequiredArgsConstructor
@Tag(name = "Coupon API", description = "Mã giảm giá")
public class CouponController {
    private final CouponService couponService;

    @PostMapping("/preview")
    @Operation(summary = "Xem trước mã giảm giá")
    public ResponseEntity<SuccessResponse<CouponPreviewResponse>> preview(
            @Valid @RequestBody CouponPreviewRequest request) {
        return ResponseEntity.ok(SuccessResponse.of(couponService.preview(request)));
    }
}
