package com.fernirx.sneakerapi.review.controller;

import com.fernirx.sneakerapi.common.response.PageResponse;
import com.fernirx.sneakerapi.common.response.SuccessResponse;
import com.fernirx.sneakerapi.common.utils.MessageUtil;
import com.fernirx.sneakerapi.review.dto.request.InternalReviewFilterRequest;
import com.fernirx.sneakerapi.review.dto.request.SetApprovedRequest;
import com.fernirx.sneakerapi.review.dto.response.ReviewInternalResponse;
import com.fernirx.sneakerapi.review.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/internal/reviews")
@RequiredArgsConstructor
@Tag(name = "Internal Review API", description = "Quản lý đánh giá (nội bộ)")
public class InternalReviewController {
    private final ReviewService reviewService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Danh sách đánh giá")
    public ResponseEntity<PageResponse<ReviewInternalResponse>> getAll(
            @ParameterObject @ModelAttribute InternalReviewFilterRequest filter,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        return ResponseEntity.ok(PageResponse.of(reviewService.getAll(filter, pageable)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Chi tiết đánh giá")
    public ResponseEntity<SuccessResponse<ReviewInternalResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(SuccessResponse.of(reviewService.getById(id)));
    }

    @PatchMapping("/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Duyệt/Ẩn đánh giá")
    public ResponseEntity<SuccessResponse<ReviewInternalResponse>> setApproved(
            @PathVariable Long id, @Valid @RequestBody SetApprovedRequest request) {
        ReviewInternalResponse response = reviewService.setApproved(id, request.approved());
        return ResponseEntity.ok(SuccessResponse.of(
                MessageUtil.getMessage("success.resource.updated", MessageUtil.getMessage("label.review")),
                response
        ));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Xóa đánh giá")
    public ResponseEntity<SuccessResponse<Void>> delete(@PathVariable Long id) {
        reviewService.delete(id);
        return ResponseEntity.ok(SuccessResponse.of(
                MessageUtil.getMessage("success.resource.deleted", MessageUtil.getMessage("label.review"))
        ));
    }
}
