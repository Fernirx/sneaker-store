package com.fernirx.sneakerapi.product.dto.request;

import jakarta.validation.constraints.NotNull;

import java.util.List;

public record AssignCollectionsRequest(
        @NotNull(message = "{validation.field.not_blank}")
        List<Long> collectionIds
) {}
