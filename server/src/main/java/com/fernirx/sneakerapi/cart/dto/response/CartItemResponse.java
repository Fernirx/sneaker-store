package com.fernirx.sneakerapi.cart.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.math.BigDecimal;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record CartItemResponse(
        Long id,
        Long variantId,
        Integer quantity,
        Integer previousQuantity,
        String productName,
        String productSlug,
        String primaryImagePublicId,
        String colorway,
        Short size,
        BigDecimal unitPrice,
        BigDecimal originalPrice,
        Boolean selected,
        Integer stockQuantity,
        Boolean outOfStock
) {}
