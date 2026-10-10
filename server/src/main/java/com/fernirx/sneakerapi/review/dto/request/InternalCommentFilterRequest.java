package com.fernirx.sneakerapi.review.dto.request;

public record InternalCommentFilterRequest(
        Long productId,
        Boolean approved
) {}
