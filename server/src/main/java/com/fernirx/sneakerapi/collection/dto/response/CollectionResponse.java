package com.fernirx.sneakerapi.collection.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDate;
import java.util.List;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

@JsonInclude(NON_NULL)
public record CollectionResponse(
        Long id,
        String name,
        String slug,
        String description,
        String imagePublicId,
        LocalDate launchDate,
        LocalDate endDate,
        List<TranslationResponse> translations
) {}
