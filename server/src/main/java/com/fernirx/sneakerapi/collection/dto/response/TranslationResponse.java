package com.fernirx.sneakerapi.collection.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

@JsonInclude(NON_NULL)
public record TranslationResponse(String locale, String name, String description) {}
