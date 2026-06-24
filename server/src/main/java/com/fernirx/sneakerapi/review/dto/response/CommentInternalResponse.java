package com.fernirx.sneakerapi.review.dto.response;

import java.time.LocalDateTime;

public record CommentInternalResponse(
        Long id,
        Long productId,
        String productName,
        Long userId,
        String userEmail,
        Long parentId,
        String content,
        Boolean approved,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
