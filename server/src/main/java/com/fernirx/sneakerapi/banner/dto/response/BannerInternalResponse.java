package com.fernirx.sneakerapi.banner.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

@JsonInclude(NON_NULL)
public record BannerInternalResponse(
        Long id,
        String title,
        String imagePublicId,
        String linkUrl,
        Integer displayOrder,
        Boolean active,
        LocalDateTime startAt,
        LocalDateTime endAt,
        LocalDateTime createdAt
) {}
