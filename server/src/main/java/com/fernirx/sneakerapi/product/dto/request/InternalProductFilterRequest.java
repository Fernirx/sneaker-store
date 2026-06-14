package com.fernirx.sneakerapi.product.dto.request;

import com.fernirx.sneakerapi.product.enums.Gender;

public record InternalProductFilterRequest(
        String search,
        Long brandId,
        Gender gender,
        Boolean active,
        Boolean newArrival,
        Boolean onSale
) {}
