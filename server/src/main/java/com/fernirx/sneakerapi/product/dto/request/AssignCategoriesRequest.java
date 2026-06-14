package com.fernirx.sneakerapi.product.dto.request;

import jakarta.validation.constraints.NotNull;

import java.util.List;

public record AssignCategoriesRequest(
        @NotNull
        List<Long> categoryIds
) {}
