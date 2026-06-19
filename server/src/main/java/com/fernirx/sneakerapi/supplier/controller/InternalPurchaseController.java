package com.fernirx.sneakerapi.supplier.controller;

import com.fernirx.sneakerapi.common.response.PageResponse;
import com.fernirx.sneakerapi.common.response.SuccessResponse;
import com.fernirx.sneakerapi.common.utils.MessageUtil;
import com.fernirx.sneakerapi.security.model.CustomUserDetails;
import com.fernirx.sneakerapi.supplier.dto.request.CancelPurchaseRequest;
import com.fernirx.sneakerapi.supplier.dto.request.CreatePurchaseRequest;
import com.fernirx.sneakerapi.supplier.dto.request.PurchaseFilterRequest;
import com.fernirx.sneakerapi.supplier.dto.request.ReceivePurchaseRequest;
import com.fernirx.sneakerapi.supplier.dto.request.UpdatePurchaseRequest;
import com.fernirx.sneakerapi.supplier.dto.response.PurchaseResponse;
import com.fernirx.sneakerapi.supplier.service.PurchaseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/internal/purchases")
@RequiredArgsConstructor
@Tag(name = "Internal Purchase API", description = "Quản lý phiếu nhập hàng (nội bộ)")
public class InternalPurchaseController {
    private final PurchaseService purchaseService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'WAREHOUSE')")
    @Operation(summary = "Danh sách phiếu nhập hàng")
    public ResponseEntity<PageResponse<PurchaseResponse>> getAll(
            @ParameterObject @ModelAttribute PurchaseFilterRequest filter,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        return ResponseEntity.ok(PageResponse.of(purchaseService.getAll(filter, pageable)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'WAREHOUSE')")
    @Operation(summary = "Chi tiết phiếu nhập hàng")
    public ResponseEntity<SuccessResponse<PurchaseResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(SuccessResponse.of(purchaseService.getById(id)));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'WAREHOUSE')")
    @Operation(summary = "Tạo phiếu nhập hàng (DRAFT)")
    public ResponseEntity<SuccessResponse<PurchaseResponse>> create(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody CreatePurchaseRequest request) {
        PurchaseResponse response = purchaseService.create(request, userDetails.getId());
        return ResponseEntity.ok(SuccessResponse.of(
                MessageUtil.getMessage("success.resource.created", MessageUtil.getMessage("label.purchase")),
                response
        ));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'WAREHOUSE')")
    @Operation(summary = "Cập nhật phiếu nhập hàng (chỉ khi đang DRAFT)")
    public ResponseEntity<SuccessResponse<PurchaseResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdatePurchaseRequest request) {
        PurchaseResponse response = purchaseService.update(id, request);
        return ResponseEntity.ok(SuccessResponse.of(
                MessageUtil.getMessage("success.resource.updated", MessageUtil.getMessage("label.purchase")),
                response
        ));
    }

    @PatchMapping("/{id}/confirm")
    @PreAuthorize("hasAnyRole('ADMIN', 'WAREHOUSE')")
    @Operation(summary = "Xác nhận phiếu nhập hàng (DRAFT -> CONFIRMED)")
    public ResponseEntity<SuccessResponse<PurchaseResponse>> confirm(@PathVariable Long id) {
        return ResponseEntity.ok(SuccessResponse.of(purchaseService.confirm(id)));
    }

    @PatchMapping("/{id}/receive")
    @PreAuthorize("hasAnyRole('ADMIN', 'WAREHOUSE')")
    @Operation(summary = "Nhận hàng (CONFIRMED -> RECEIVED), cộng kho theo từng item")
    public ResponseEntity<SuccessResponse<PurchaseResponse>> receive(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id,
            @Valid @RequestBody ReceivePurchaseRequest request) {
        PurchaseResponse response = purchaseService.receive(id, request, userDetails.getId());
        return ResponseEntity.ok(SuccessResponse.of(response));
    }

    @PatchMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('ADMIN', 'WAREHOUSE')")
    @Operation(summary = "Hủy phiếu nhập hàng (DRAFT/CONFIRMED -> CANCELLED)")
    public ResponseEntity<SuccessResponse<PurchaseResponse>> cancel(
            @PathVariable Long id,
            @RequestBody(required = false) CancelPurchaseRequest request) {
        return ResponseEntity.ok(SuccessResponse.of(purchaseService.cancel(id, request)));
    }

    @PatchMapping("/{id}/mark-paid")
    @PreAuthorize("hasAnyRole('ADMIN', 'WAREHOUSE')")
    @Operation(summary = "Đánh dấu đã thanh toán cho nhà cung cấp")
    public ResponseEntity<SuccessResponse<PurchaseResponse>> markAsPaid(@PathVariable Long id) {
        return ResponseEntity.ok(SuccessResponse.of(purchaseService.markAsPaid(id)));
    }
}
