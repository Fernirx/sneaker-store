package com.fernirx.sneakerapi.shipping.dto;

import com.fernirx.sneakerapi.product.entity.ProductVariant;

public record ParcelItem(
        String name,
        String code,
        Integer quantity,
        Integer weight,
        Integer length,
        Integer width,
        Integer height
) {
    public static ParcelItem from(ProductVariant variant, Integer quantity) {
        return new ParcelItem(
                variant.getProduct().getName(),
                variant.getSku(),
                quantity,
                variant.getWeight(),
                variant.getLength(),
                variant.getWidth(),
                variant.getHeight()
        );
    }
}
