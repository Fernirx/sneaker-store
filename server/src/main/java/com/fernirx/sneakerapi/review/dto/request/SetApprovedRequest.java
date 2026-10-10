package com.fernirx.sneakerapi.review.dto.request;

import jakarta.validation.constraints.NotNull;

public record SetApprovedRequest(
        @NotNull(message = "{validation.field.not_blank}")
        Boolean approved
) {}
