package com.fernirx.sneakerapi.returns.controller;

import com.fernirx.sneakerapi.common.response.PageResponse;
import com.fernirx.sneakerapi.common.response.SuccessResponse;
import com.fernirx.sneakerapi.common.utils.MessageUtil;
import com.fernirx.sneakerapi.returns.dto.request.ProcessReturnRequest;
import com.fernirx.sneakerapi.returns.dto.request.RejectReturnRequest;
import com.fernirx.sneakerapi.returns.dto.request.ReturnFilterRequest;
import com.fernirx.sneakerapi.returns.dto.response.ReturnRequestInternalResponse;
import com.fernirx.sneakerapi.returns.service.ReturnRequestService;
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
@RequestMapping("/internal/returns")
@RequiredArgsConstructor
@Tag(name = "Internal Return Request API", description = "Quản lý yêu cầu đổi/trả hàng (nội bộ)")
public class InternalReturnRequestController {
    private final ReturnRequestService returnRequestService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SALE', 'WAREHOUSE')")
    @Operation(summary = "Danh sách yêu cầu đổi/trả hàng")
    public ResponseEntity<PageResponse<ReturnRequestInternalResponse>> getAll(
            @ParameterObject @ModelAttribute ReturnFilterRequest filter,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        return ResponseEntity.ok(PageResponse.of(returnRequestService.getAll(filter, pageable)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SALE', 'WAREHOUSE')")
    @Operation(summary = "Chi tiết yêu cầu đổi/trả hàng")
    public ResponseEntity<SuccessResponse<ReturnRequestInternalResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(SuccessResponse.of(returnRequestService.getById(id)));
    }

    @PatchMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('ADMIN', 'SALE')")
    @Operation(summary = "Duyệt yêu cầu đổi/trả (PENDING -> APPROVED)")
    public ResponseEntity<SuccessResponse<ReturnRequestInternalResponse>> approve(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id) {
        ReturnRequestInternalResponse response = returnRequestService.approve(id, userDetails.getId());
        return ResponseEntity.ok(SuccessResponse.of(MessageUtil.getMessage("success.return.approved"), response));
    }

    @PatchMapping("/{id}/reject")
    @PreAuthorize("hasAnyRole('ADMIN', 'SALE')")
    @Operation(summary = "Từ chối yêu cầu đổi/trả (PENDING/APPROVED -> REJECTED)")
    public ResponseEntity<SuccessResponse<ReturnRequestInternalResponse>> reject(
            @PathVariable Long id,
            @Valid @RequestBody RejectReturnRequest request) {
        ReturnRequestInternalResponse response = returnRequestService.reject(id, request);
        return ResponseEntity.ok(SuccessResponse.of(MessageUtil.getMessage("success.return.rejected"), response));
    }

    @PatchMapping("/{id}/received")
    @PreAuthorize("hasAnyRole('ADMIN', 'WAREHOUSE')")
    @Operation(summary = "Xác nhận đã nhận hàng trả về tại kho (APPROVED -> RECEIVED)")
    public ResponseEntity<SuccessResponse<ReturnRequestInternalResponse>> markReceived(@PathVariable Long id) {
        ReturnRequestInternalResponse response = returnRequestService.markReceived(id);
        return ResponseEntity.ok(SuccessResponse.of(MessageUtil.getMessage("success.return.received"), response));
    }

    @PatchMapping("/{id}/process")
    @PreAuthorize("hasAnyRole('ADMIN', 'WAREHOUSE')")
    @Operation(summary = "Xử lý kết quả kiểm tra hàng trả về (RECEIVED -> COMPLETED / REJECTED_AFTER_INSPECTION)")
    public ResponseEntity<SuccessResponse<ReturnRequestInternalResponse>> process(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id,
            @Valid @RequestBody ProcessReturnRequest request) {
        ReturnRequestInternalResponse response = returnRequestService.process(id, userDetails.getId(), request);
        return ResponseEntity.ok(SuccessResponse.of(MessageUtil.getMessage("success.return.processed"), response));
    }

    @PatchMapping("/{id}/retry-shipment")
    @PreAuthorize("hasAnyRole('ADMIN', 'WAREHOUSE')")
    @Operation(summary = "Thử tạo lại vận đơn GHN cho hàng đổi (khi lần tạo trước đó thất bại)")
    public ResponseEntity<SuccessResponse<ReturnRequestInternalResponse>> retryExchangeShipment(@PathVariable Long id) {
        ReturnRequestInternalResponse response = returnRequestService.retryExchangeShipment(id);
        return ResponseEntity.ok(SuccessResponse.of(MessageUtil.getMessage("success.return.shipment_retried"), response));
    }
}
