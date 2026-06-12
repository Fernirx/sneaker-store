package com.fernirx.sneakerapi.collection.dto.request;

import com.fernirx.sneakerapi.common.annotation.NullableNotBlank;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.List;

public record CreateCollectionRequest(
        @NotBlank @Size(max = 100) String name,
        @NullableNotBlank String description,
        @NullableNotBlank @Size(max = 255) String imagePublicId,
        LocalDate launchDate,
        LocalDate endDate,
        List<TranslationRequest> translations
) {}
