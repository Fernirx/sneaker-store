package com.fernirx.sneakerapi.review.dto.response;

public record ReviewImageResponse(
        Long id,
        String imagePublicId,
        Integer displayOrder
) {}
