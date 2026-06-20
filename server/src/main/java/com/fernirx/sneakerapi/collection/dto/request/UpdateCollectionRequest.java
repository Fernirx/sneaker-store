package com.fernirx.sneakerapi.collection.dto.request;

import com.fernirx.sneakerapi.common.annotation.NullableNotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record UpdateCollectionRequest(
        @NullableNotBlank @Size(max = 100, message = "{validation.size.max}") String name,
        @NullableNotBlank String description,
        @NullableNotBlank @Size(max = 255, message = "{validation.size.max}") String imagePublicId,
        LocalDate launchDate,
        LocalDate endDate,
        Boolean active
) {}
