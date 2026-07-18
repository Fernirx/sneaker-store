package com.fernirx.sneakerapi.banner.dto.request;

import com.fernirx.sneakerapi.common.annotation.NullableNotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record UpdateBannerRequest(
        @NullableNotBlank @Size(max = 150, message = "{validation.size.max}") String title,
        @NullableNotBlank @Size(max = 255, message = "{validation.size.max}") String imagePublicId,
        @NullableNotBlank @Size(max = 500, message = "{validation.size.max}") String linkUrl,
        Integer displayOrder,
        LocalDateTime startAt,
        LocalDateTime endAt,
        Boolean active
) {}
