package com.fernirx.sneakerapi.product.dto.request;

import com.fernirx.sneakerapi.product.enums.Gender;

import java.math.BigDecimal;

public record BySlugProductFilterRequest(
        String search,
        Gender gender,
        BigDecimal minPrice,
        BigDecimal maxPrice,
        Boolean newArrival,
        Boolean onSale
) {}
