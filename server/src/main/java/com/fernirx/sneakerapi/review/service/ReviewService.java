package com.fernirx.sneakerapi.review.service;

import com.fernirx.sneakerapi.review.dto.request.CreateReviewRequest;
import com.fernirx.sneakerapi.review.dto.request.InternalReviewFilterRequest;
import com.fernirx.sneakerapi.review.dto.request.UpdateReviewRequest;
import com.fernirx.sneakerapi.review.dto.response.ReviewInternalResponse;
import com.fernirx.sneakerapi.review.dto.response.ReviewResponse;
import com.fernirx.sneakerapi.review.dto.response.ReviewSummaryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ReviewService {

    // Public
    Page<ReviewResponse> getApprovedReviews(String productSlug, Pageable pageable);
    ReviewSummaryResponse getReviewSummary(String productSlug);

    // Me
    ReviewResponse createReview(Long userId, CreateReviewRequest request);
    ReviewResponse updateReview(Long userId, Long reviewId, UpdateReviewRequest request);
    void deleteReview(Long userId, Long reviewId);

    // Internal
    Page<ReviewInternalResponse> getAll(InternalReviewFilterRequest filter, Pageable pageable);
    ReviewInternalResponse getById(Long id);
    ReviewInternalResponse setApproved(Long id, boolean approved);
    void delete(Long id);
}
