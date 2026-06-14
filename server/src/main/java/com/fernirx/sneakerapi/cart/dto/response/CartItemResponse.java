package com.fernirx.sneakerapi.cart.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.math.BigDecimal;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record CartItemResponse(
        Long id,
        Long variantId,
        Integer quantity,
        String productName,
        String productSlug,
        String primaryImagePublicId,
        String colorway,
        Short size,
        BigDecimal unitPrice,
        Integer stockQuantity,
        Boolean outOfStock
) {}
