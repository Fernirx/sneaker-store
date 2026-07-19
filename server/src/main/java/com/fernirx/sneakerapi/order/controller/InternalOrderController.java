package com.fernirx.sneakerapi.order.controller;

import com.fernirx.sneakerapi.common.response.PageResponse;
import com.fernirx.sneakerapi.common.response.SuccessResponse;
import com.fernirx.sneakerapi.order.dto.request.OrderFilterRequest;
import com.fernirx.sneakerapi.order.dto.request.UpdateOrderStatusRequest;
import com.fernirx.sneakerapi.order.dto.response.OrderInternalResponse;
import com.fernirx.sneakerapi.order.dto.response.OrderStatusHistoryResponse;
import com.fernirx.sneakerapi.order.service.OrderService;
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
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/internal/orders")
@RequiredArgsConstructor
@Tag(name = "Internal Order API", description = "Quản lý đơn hàng (nội bộ)")
public class InternalOrderController {
    private final OrderService orderService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SALE', 'WAREHOUSE')")
    @Operation(summary = "Danh sách đơn hàng")
    public ResponseEntity<PageResponse<OrderInternalResponse>> getAll(
            @ParameterObject @ModelAttribute OrderFilterRequest filter,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        return ResponseEntity.ok(PageResponse.of(orderService.getAll(filter, pageable)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SALE', 'WAREHOUSE')")
    @Operation(summary = "Chi tiết đơn hàng")
    public ResponseEntity<SuccessResponse<OrderInternalResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(SuccessResponse.of(orderService.getById(id)));
    }

    @GetMapping("/{id}/history")
    @PreAuthorize("hasAnyRole('ADMIN', 'SALE', 'WAREHOUSE')")
    @Operation(summary = "Lịch sử trạng thái đơn hàng")
    public ResponseEntity<SuccessResponse<List<OrderStatusHistoryResponse>>> getHistory(@PathVariable Long id) {
        return ResponseEntity.ok(SuccessResponse.of(orderService.getHistory(id)));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'SALE', 'WAREHOUSE')")
    @Operation(summary = "Cập nhật trạng thái đơn hàng")
    public ResponseEntity<SuccessResponse<OrderInternalResponse>> updateStatus(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id,
            @Valid @RequestBody UpdateOrderStatusRequest request) {
        Collection<String> callerRoles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());
        OrderInternalResponse response = orderService.updateStatus(id, request, userDetails.getId(), callerRoles);
        return ResponseEntity.ok(SuccessResponse.of(response));
    }

    @PostMapping("/{id}/shipment")
    @PreAuthorize("hasAnyRole('ADMIN', 'WAREHOUSE')")
    @Operation(summary = "Tạo vận đơn GHN cho đơn hàng (chỉ khi đơn đã CONFIRMED)")
    public ResponseEntity<SuccessResponse<OrderInternalResponse>> createShipment(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id) {
        OrderInternalResponse response = orderService.createShipment(id, userDetails.getId());
        return ResponseEntity.ok(SuccessResponse.of(response));
    }

    @DeleteMapping("/{id}/shipment")
    @PreAuthorize("hasAnyRole('ADMIN', 'WAREHOUSE')")
    @Operation(summary = "Hủy vận đơn GHN của đơn hàng (đơn quay lại trạng thái CONFIRMED)")
    public ResponseEntity<SuccessResponse<OrderInternalResponse>> cancelShipment(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id) {
        OrderInternalResponse response = orderService.cancelShipment(id, userDetails.getId());
        return ResponseEntity.ok(SuccessResponse.of(response));
    }

    @PostMapping("/{id}/shipment/sync")
    @PreAuthorize("hasAnyRole('ADMIN', 'WAREHOUSE')")
    @Operation(summary = "Đồng bộ trạng thái vận đơn GHN mới nhất cho đơn hàng")
    public ResponseEntity<SuccessResponse<OrderInternalResponse>> syncShipmentStatus(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id) {
        OrderInternalResponse response = orderService.syncShipmentStatus(id, userDetails.getId());
        return ResponseEntity.ok(SuccessResponse.of(response));
    }
}
