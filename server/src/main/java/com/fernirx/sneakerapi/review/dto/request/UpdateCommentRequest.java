package com.fernirx.sneakerapi.review.dto.request;

import jakarta.validation.constraints.NotBlank;

public record UpdateCommentRequest(
        @NotBlank(message = "{validation.field.not_blank}")
        String content
) {}
