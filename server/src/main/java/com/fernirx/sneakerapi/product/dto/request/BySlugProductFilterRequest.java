package com.fernirx.sneakerapi.product.dto.request;

import com.fernirx.sneakerapi.product.enums.Gender;

import java.math.BigDecimal;
import java.util.List;

public record BySlugProductFilterRequest(
        String search,
        Gender gender,
        List<String> brandSlugs,
        BigDecimal minPrice,
        BigDecimal maxPrice,
        Boolean newArrival,
        Boolean onSale
) {}
