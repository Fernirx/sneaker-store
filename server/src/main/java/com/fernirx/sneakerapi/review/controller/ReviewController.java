package com.fernirx.sneakerapi.review.controller;

import com.fernirx.sneakerapi.common.response.SuccessResponse;
import com.fernirx.sneakerapi.common.utils.MessageUtil;
import com.fernirx.sneakerapi.review.dto.request.CreateReviewRequest;
import com.fernirx.sneakerapi.review.dto.request.UpdateReviewRequest;
import com.fernirx.sneakerapi.review.dto.response.ReviewResponse;
import com.fernirx.sneakerapi.review.service.ReviewService;
import com.fernirx.sneakerapi.security.model.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/me/reviews")
@RequiredArgsConstructor
@Tag(name = "My Review API", description = "Quản lý đánh giá của tôi")
public class ReviewController {
    private final ReviewService reviewService;

    @PostMapping
    @Operation(summary = "Viết đánh giá sản phẩm")
    public ResponseEntity<SuccessResponse<ReviewResponse>> create(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody CreateReviewRequest request) {
        ReviewResponse response = reviewService.createReview(userDetails.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(SuccessResponse.of(
                MessageUtil.getMessage("success.resource.created", MessageUtil.getMessage("label.review")),
                response
        ));
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Cập nhật đánh giá của tôi")
    public ResponseEntity<SuccessResponse<ReviewResponse>> update(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id,
            @Valid @RequestBody UpdateReviewRequest request) {
        ReviewResponse response = reviewService.updateReview(userDetails.getId(), id, request);
        return ResponseEntity.ok(SuccessResponse.of(
                MessageUtil.getMessage("success.resource.updated", MessageUtil.getMessage("label.review")),
                response
        ));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Xóa đánh giá của tôi")
    public ResponseEntity<SuccessResponse<Void>> delete(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id) {
        reviewService.deleteReview(userDetails.getId(), id);
        return ResponseEntity.ok(SuccessResponse.of(
                MessageUtil.getMessage("success.resource.deleted", MessageUtil.getMessage("label.review"))
        ));
    }
}
