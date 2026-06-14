package com.fernirx.sneakerapi.collection.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SlugUpdateRequest(
        @NotBlank @Size(max = 100) String slug
) {}
