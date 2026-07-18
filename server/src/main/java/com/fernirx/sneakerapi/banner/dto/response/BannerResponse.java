package com.fernirx.sneakerapi.banner.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

@JsonInclude(NON_NULL)
public record BannerResponse(
        Long id,
        String title,
        String imagePublicId,
        String linkUrl
) {}
