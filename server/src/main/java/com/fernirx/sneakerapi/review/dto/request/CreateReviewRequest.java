package com.fernirx.sneakerapi.review.dto.request;

import com.fernirx.sneakerapi.common.annotation.NullableNotBlank;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record CreateReviewRequest(
        @NotNull(message = "{validation.field.not_blank}")
        Long productId,

        @NotNull(message = "{validation.field.not_blank}")
        @Min(value = 1, message = "{validation.number.min}")
        @Max(value = 5, message = "{validation.number.max}")
        Short rating,

        @NullableNotBlank
        @Size(max = 255, message = "{validation.size.max}")
        String title,

        @NullableNotBlank
        String comment,

        @Size(max = 5, message = "{validation.list.max}")
        List<@NotBlank(message = "{validation.field.not_blank}") String> imagePublicIds
) {}
