package com.fernirx.sneakerapi.customer.controller;

import com.fernirx.sneakerapi.common.response.PageResponse;
import com.fernirx.sneakerapi.common.response.SuccessResponse;
import com.fernirx.sneakerapi.common.utils.MessageUtil;
import com.fernirx.sneakerapi.customer.dto.request.CreateWishlistRequest;
import com.fernirx.sneakerapi.customer.dto.response.WishlistResponse;
import com.fernirx.sneakerapi.customer.service.WishlistService;
import com.fernirx.sneakerapi.security.model.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/me/wishlist")
@RequiredArgsConstructor
@Tag(name = "Wishlist API", description = "Quản lý sản phẩm yêu thích")
public class WishlistController {
    private final WishlistService wishlistService;

    @GetMapping
    @Operation(summary = "Lấy danh sách sản phẩm yêu thích")
    public ResponseEntity<PageResponse<WishlistResponse>> getWishlist(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(PageResponse.of(wishlistService.getWishlist(userDetails.getId(), pageable)));
    }

    @PostMapping
    @Operation(summary = "Thêm sản phẩm vào yêu thích")
    public ResponseEntity<SuccessResponse<WishlistResponse>> addWishlist(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody CreateWishlistRequest request) {
        WishlistResponse response = wishlistService.addWishlist(userDetails.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(SuccessResponse.of(
                MessageUtil.getMessage("success.resource.created", MessageUtil.getMessage("label.wishlist")),
                response
        ));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Xóa sản phẩm khỏi yêu thích")
    public ResponseEntity<SuccessResponse<Void>> removeWishlist(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id) {
        wishlistService.removeWishlist(userDetails.getId(), id);
        return ResponseEntity.ok(SuccessResponse.of(
                MessageUtil.getMessage("success.resource.deleted", MessageUtil.getMessage("label.wishlist"))
        ));
    }
}
