package com.fernirx.sneakerapi.review.dto.response;

import java.util.Map;

public record ReviewSummaryResponse(
        Double averageRating,
        Long totalReviews,
        Map<Short, Long> distribution
) {}
