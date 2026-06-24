package com.fernirx.sneakerapi.review.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public record ReviewResponse(
        Long id,
        Short rating,
        String title,
        String comment,
        ReviewerResponse user,
        List<ReviewImageResponse> images,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
