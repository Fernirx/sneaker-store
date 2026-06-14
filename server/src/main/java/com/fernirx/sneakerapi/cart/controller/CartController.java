package com.fernirx.sneakerapi.cart.controller;

import com.fernirx.sneakerapi.cart.dto.request.AddCartItemRequest;
import com.fernirx.sneakerapi.cart.dto.request.MergeCartRequest;
import com.fernirx.sneakerapi.cart.dto.request.UpdateCartItemRequest;
import com.fernirx.sneakerapi.cart.dto.response.CartResponse;
import com.fernirx.sneakerapi.cart.service.CartService;
import com.fernirx.sneakerapi.common.response.SuccessResponse;
import com.fernirx.sneakerapi.common.utils.MessageUtil;
import com.fernirx.sneakerapi.security.model.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cart")
@RequiredArgsConstructor
@Tag(name = "Cart API", description = "Giỏ hàng — hỗ trợ cả user và guest")
public class CartController {

    private final CartService cartService;

    @GetMapping
    @Operation(summary = "Lấy giỏ hàng hiện tại")
    public ResponseEntity<SuccessResponse<CartResponse>> getCart(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestHeader(value = "X-Guest-Token", required = false) String guestToken) {
        Long userId = userId(userDetails);
        return ResponseEntity.ok(SuccessResponse.of(cartService.getCart(userId, guestToken)));
    }

    @PostMapping("/items")
    @Operation(summary = "Thêm sản phẩm vào giỏ")
    public ResponseEntity<SuccessResponse<CartResponse>> addItem(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestHeader(value = "X-Guest-Token", required = false) String guestToken,
            @Valid @RequestBody AddCartItemRequest request) {
        Long userId = userId(userDetails);
        return ResponseEntity.ok(SuccessResponse.of(cartService.addItem(userId, guestToken, request)));
    }

    @PatchMapping("/items/{itemId}")
    @Operation(summary = "Cập nhật số lượng sản phẩm trong giỏ")
    public ResponseEntity<SuccessResponse<CartResponse>> updateItem(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestHeader(value = "X-Guest-Token", required = false) String guestToken,
            @PathVariable Long itemId,
            @Valid @RequestBody UpdateCartItemRequest request) {
        Long userId = userId(userDetails);
        return ResponseEntity.ok(SuccessResponse.of(cartService.updateItem(userId, guestToken, itemId, request)));
    }

    @DeleteMapping("/items/{itemId}")
    @Operation(summary = "Xóa sản phẩm khỏi giỏ")
    public ResponseEntity<SuccessResponse<CartResponse>> removeItem(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestHeader(value = "X-Guest-Token", required = false) String guestToken,
            @PathVariable Long itemId) {
        Long userId = userId(userDetails);
        return ResponseEntity.ok(SuccessResponse.of(cartService.removeItem(userId, guestToken, itemId)));
    }

    @DeleteMapping
    @Operation(summary = "Xóa toàn bộ giỏ hàng")
    public ResponseEntity<SuccessResponse<CartResponse>> clearCart(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestHeader(value = "X-Guest-Token", required = false) String guestToken) {
        Long userId = userId(userDetails);
        return ResponseEntity.ok(SuccessResponse.of(
                MessageUtil.getMessage("success.cart.cleared"),
                cartService.clearCart(userId, guestToken)
        ));
    }

    @PostMapping("/merge")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Merge giỏ hàng guest vào user sau đăng nhập")
    public ResponseEntity<SuccessResponse<CartResponse>> mergeGuestCart(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody MergeCartRequest request) {
        return ResponseEntity.ok(SuccessResponse.of(
                MessageUtil.getMessage("success.cart.merged"),
                cartService.mergeGuestCart(userDetails.getId(), request.guestToken())
        ));
    }

    private Long userId(CustomUserDetails userDetails) {
        return userDetails != null ? userDetails.getId() : null;
    }
}
