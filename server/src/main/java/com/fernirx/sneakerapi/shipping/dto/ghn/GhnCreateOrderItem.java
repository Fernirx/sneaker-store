package com.fernirx.sneakerapi.shipping.dto.ghn;

public record GhnCreateOrderItem(
        String name,
        String code,
        Integer quantity,
        Long price
) {}
