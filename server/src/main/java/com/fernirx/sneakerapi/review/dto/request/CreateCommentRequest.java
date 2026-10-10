package com.fernirx.sneakerapi.review.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateCommentRequest(
        @NotNull(message = "{validation.field.not_blank}")
        Long productId,

        Long parentId,

        @NotBlank(message = "{validation.field.not_blank}")
        String content
) {}
