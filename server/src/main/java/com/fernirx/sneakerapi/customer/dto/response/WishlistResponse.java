package com.fernirx.sneakerapi.customer.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record WishlistResponse(
        Long id,
        Long productId,
        String productName,
        String productSlug,
        String brandName,
        String primaryImagePublicId,
        Long variantId,
        String colorway,
        String colorHex,
        Short size,
        BigDecimal price,
        BigDecimal originalPrice,
        Boolean outOfStock,
        LocalDateTime createdAt
) {}
