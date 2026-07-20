package com.fernirx.sneakerapi.order.controller;

import com.fernirx.sneakerapi.common.response.PageResponse;
import com.fernirx.sneakerapi.common.response.SuccessResponse;
import com.fernirx.sneakerapi.common.utils.MessageUtil;
import com.fernirx.sneakerapi.order.dto.request.CreateOrderRequest;
import com.fernirx.sneakerapi.order.dto.request.GuestOtpRequest;
import com.fernirx.sneakerapi.order.dto.response.OrderResponse;
import com.fernirx.sneakerapi.order.dto.response.OrderStatusHistoryResponse;
import com.fernirx.sneakerapi.order.service.OrderService;
import com.fernirx.sneakerapi.security.model.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
@Tag(name = "Order API", description = "Đặt hàng — hỗ trợ cả user và guest")
public class OrderController {
    private final OrderService orderService;

    @PostMapping("/guest/otp")
    @Operation(summary = "Gửi OTP xác minh email cho guest đặt hàng")
    public ResponseEntity<SuccessResponse<Void>> sendGuestOtp(@Valid @RequestBody GuestOtpRequest request) {
        orderService.sendGuestOtp(request.email());
        return ResponseEntity.ok(SuccessResponse.of(MessageUtil.getMessage("success.order.guest_otp_sent")));
    }

    @PostMapping
    @Operation(summary = "Tạo đơn hàng")
    public ResponseEntity<SuccessResponse<OrderResponse>> createOrder(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestHeader(value = "X-Guest-Token", required = false) String guestToken,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            @Valid @RequestBody CreateOrderRequest request) {
        OrderResponse response = orderService.createOrder(userId(userDetails), guestToken, idempotencyKey, request);
        return ResponseEntity.ok(SuccessResponse.of(MessageUtil.getMessage("success.order.created"), response));
    }

    @GetMapping
    @Operation(summary = "Danh sách đơn hàng của tôi")
    public ResponseEntity<PageResponse<OrderResponse>> getMyOrders(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestHeader(value = "X-Guest-Token", required = false) String guestToken,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        return ResponseEntity.ok(PageResponse.of(
                orderService.getMyOrders(userId(userDetails), guestToken, pageable)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Chi tiết đơn hàng của tôi")
    public ResponseEntity<SuccessResponse<OrderResponse>> getMyOrderDetail(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestHeader(value = "X-Guest-Token", required = false) String guestToken,
            @PathVariable Long id) {
        return ResponseEntity.ok(SuccessResponse.of(
                orderService.getMyOrderDetail(id, userId(userDetails), guestToken)));
    }

    @GetMapping("/{id}/history")
    @Operation(summary = "Lịch sử trạng thái đơn hàng của tôi")
    public ResponseEntity<SuccessResponse<List<OrderStatusHistoryResponse>>> getMyOrderHistory(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestHeader(value = "X-Guest-Token", required = false) String guestToken,
            @PathVariable Long id) {
        return ResponseEntity.ok(SuccessResponse.of(
                orderService.getMyOrderHistory(id, userId(userDetails), guestToken)));
    }

    @PatchMapping("/{id}/cancel")
    @Operation(summary = "Hủy đơn hàng (chỉ khi còn PENDING)")
    public ResponseEntity<SuccessResponse<Void>> cancelMyOrder(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestHeader(value = "X-Guest-Token", required = false) String guestToken,
            @PathVariable Long id) {
        orderService.customerCancelOrder(id, userId(userDetails), guestToken);
        return ResponseEntity.ok(SuccessResponse.of(MessageUtil.getMessage("success.order.cancelled")));
    }

    private Long userId(CustomUserDetails userDetails) {
        return userDetails != null ? userDetails.getId() : null;
    }
}
