package com.fernirx.sneakerapi.cart.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.math.BigDecimal;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record CartResponse(
        String guestToken,
        List<CartItemResponse> items,
        int totalItems,
        BigDecimal totalAmount,
        BigDecimal tierDiscountAmount,
        Integer tierDiscountRate,
        String tierName
) {}
