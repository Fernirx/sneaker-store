package com.fernirx.sneakerapi.review.dto.request;

public record InternalReviewFilterRequest(
        Long productId,
        Short rating,
        Boolean approved
) {}
