package com.fernirx.sneakerapi.collection.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

@JsonInclude(NON_NULL)
public record CollectionInternalResponse(
        Long id,
        String name,
        String slug,
        String description,
        String imagePublicId,
        LocalDate launchDate,
        LocalDate endDate,
        Boolean active,
        LocalDateTime createdAt,
        List<TranslationResponse> translations
) {}
