package com.fernirx.sneakerapi.inventory.controller;

import com.fernirx.sneakerapi.common.response.PageResponse;
import com.fernirx.sneakerapi.common.response.SuccessResponse;
import com.fernirx.sneakerapi.common.utils.MessageUtil;
import com.fernirx.sneakerapi.inventory.dto.request.CancelStockAdjustmentRequest;
import com.fernirx.sneakerapi.inventory.dto.request.CreateStockAdjustmentRequest;
import com.fernirx.sneakerapi.inventory.dto.request.StockAdjustmentFilterRequest;
import com.fernirx.sneakerapi.inventory.dto.request.UpdateStockAdjustmentRequest;
import com.fernirx.sneakerapi.inventory.dto.response.StockAdjustmentResponse;
import com.fernirx.sneakerapi.inventory.service.StockAdjustmentService;
import com.fernirx.sneakerapi.security.model.CustomUserDetails;
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
@RequestMapping("/internal/stock-adjustments")
@RequiredArgsConstructor
@Tag(name = "Internal Stock Adjustment API", description = "Quản lý phiếu điều chỉnh kho (nội bộ)")
public class InternalStockAdjustmentController {
    private final StockAdjustmentService stockAdjustmentService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'WAREHOUSE')")
    @Operation(summary = "Danh sách phiếu điều chỉnh kho")
    public ResponseEntity<PageResponse<StockAdjustmentResponse>> getAll(
            @ParameterObject @ModelAttribute StockAdjustmentFilterRequest filter,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        return ResponseEntity.ok(PageResponse.of(stockAdjustmentService.getAll(filter, pageable)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'WAREHOUSE')")
    @Operation(summary = "Chi tiết phiếu điều chỉnh kho")
    public ResponseEntity<SuccessResponse<StockAdjustmentResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(SuccessResponse.of(stockAdjustmentService.getById(id)));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'WAREHOUSE')")
    @Operation(summary = "Tạo phiếu điều chỉnh kho (DRAFT)")
    public ResponseEntity<SuccessResponse<StockAdjustmentResponse>> create(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody CreateStockAdjustmentRequest request) {
        StockAdjustmentResponse response = stockAdjustmentService.create(request, userDetails.getId());
        return ResponseEntity.ok(SuccessResponse.of(
                MessageUtil.getMessage("success.resource.created", MessageUtil.getMessage("label.stock_adjustment")),
                response
        ));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'WAREHOUSE')")
    @Operation(summary = "Cập nhật phiếu điều chỉnh kho (chỉ khi đang DRAFT)")
    public ResponseEntity<SuccessResponse<StockAdjustmentResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateStockAdjustmentRequest request) {
        StockAdjustmentResponse response = stockAdjustmentService.update(id, request);
        return ResponseEntity.ok(SuccessResponse.of(
                MessageUtil.getMessage("success.resource.updated", MessageUtil.getMessage("label.stock_adjustment")),
                response
        ));
    }

    @PatchMapping("/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Duyệt phiếu điều chỉnh kho (DRAFT -> APPROVED, chỉ ADMIN)")
    public ResponseEntity<SuccessResponse<StockAdjustmentResponse>> approve(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id) {
        StockAdjustmentResponse response = stockAdjustmentService.approve(id, userDetails.getId());
        return ResponseEntity.ok(SuccessResponse.of(response));
    }

    @PatchMapping("/{id}/confirm")
    @PreAuthorize("hasAnyRole('ADMIN', 'WAREHOUSE')")
    @Operation(summary = "Áp dụng phiếu vào kho (APPROVED -> CONFIRMED)")
    public ResponseEntity<SuccessResponse<StockAdjustmentResponse>> confirm(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id) {
        StockAdjustmentResponse response = stockAdjustmentService.confirm(id, userDetails.getId());
        return ResponseEntity.ok(SuccessResponse.of(response));
    }

    @PatchMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('ADMIN', 'WAREHOUSE')")
    @Operation(summary = "Hủy phiếu điều chỉnh kho (DRAFT/APPROVED -> CANCELLED)")
    public ResponseEntity<SuccessResponse<StockAdjustmentResponse>> cancel(
            @PathVariable Long id,
            @RequestBody(required = false) CancelStockAdjustmentRequest request) {
        return ResponseEntity.ok(SuccessResponse.of(stockAdjustmentService.cancel(id, request)));
    }
}
