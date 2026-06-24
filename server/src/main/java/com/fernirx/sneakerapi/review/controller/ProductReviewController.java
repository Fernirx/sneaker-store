package com.fernirx.sneakerapi.review.controller;

import com.fernirx.sneakerapi.common.response.PageResponse;
import com.fernirx.sneakerapi.common.response.SuccessResponse;
import com.fernirx.sneakerapi.review.dto.response.ReviewResponse;
import com.fernirx.sneakerapi.review.dto.response.ReviewSummaryResponse;
import com.fernirx.sneakerapi.review.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/products/{slug}/reviews")
@RequiredArgsConstructor
@Tag(name = "Product Review API", description = "Đánh giá sản phẩm (công khai)")
public class ProductReviewController {
    private final ReviewService reviewService;

    @GetMapping
    @Operation(summary = "Danh sách đánh giá đã duyệt của sản phẩm")
    public ResponseEntity<PageResponse<ReviewResponse>> getReviews(
            @PathVariable String slug,
            @PageableDefault(size = 10, sort = "createdAt") Pageable pageable) {
        return ResponseEntity.ok(PageResponse.of(reviewService.getApprovedReviews(slug, pageable)));
    }

    @GetMapping("/summary")
    @Operation(summary = "Tổng hợp đánh giá (điểm trung bình + phân bố theo sao)")
    public ResponseEntity<SuccessResponse<ReviewSummaryResponse>> getSummary(@PathVariable String slug) {
        return ResponseEntity.ok(SuccessResponse.of(reviewService.getReviewSummary(slug)));
    }
}
