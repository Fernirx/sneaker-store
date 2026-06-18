package com.fernirx.sneakerapi.payment.controller;

import com.fernirx.sneakerapi.common.response.PageResponse;
import com.fernirx.sneakerapi.common.response.SuccessResponse;
import com.fernirx.sneakerapi.payment.dto.request.PaymentFilterRequest;
import com.fernirx.sneakerapi.payment.dto.response.PaymentInternalResponse;
import com.fernirx.sneakerapi.payment.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/internal/payments")
@RequiredArgsConstructor
@Tag(name = "Internal Payment API", description = "Quản lý giao dịch thanh toán (nội bộ)")
public class InternalPaymentController {
    private final PaymentService paymentService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SALE')")
    @Operation(summary = "Danh sách giao dịch thanh toán")
    public ResponseEntity<PageResponse<PaymentInternalResponse>> getAll(
            @ParameterObject @ModelAttribute PaymentFilterRequest filter,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        return ResponseEntity.ok(PageResponse.of(paymentService.getAll(filter, pageable)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SALE')")
    @Operation(summary = "Chi tiết giao dịch thanh toán")
    public ResponseEntity<SuccessResponse<PaymentInternalResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(SuccessResponse.of(paymentService.getById(id)));
    }
}
