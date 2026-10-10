package com.fernirx.sneakerapi.review.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public record ReviewInternalResponse(
        Long id,
        Long productId,
        String productName,
        Long userId,
        String userEmail,
        Long orderId,
        String orderCode,
        Short rating,
        String title,
        String comment,
        Boolean approved,
        List<ReviewImageResponse> images,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
