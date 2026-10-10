package com.fernirx.sneakerapi.shipping.dto;

import com.fernirx.sneakerapi.order.entity.OrderItem;
import com.fernirx.sneakerapi.product.entity.ProductVariant;

import java.math.BigDecimal;

public record ParcelItem(
        String name,
        String code,
        Integer quantity,
        Integer weight,
        Integer length,
        Integer width,
        Integer height,
        BigDecimal price
) {
    public static ParcelItem from(ProductVariant variant, Integer quantity) {
        return new ParcelItem(
                variant.getProduct().getName(),
                variant.getSku(),
                quantity,
                variant.getWeight(),
                variant.getLength(),
                variant.getWidth(),
                variant.getHeight(),
                null
        );
    }

    /** Dùng cho tạo vận đơn GHN thật — lấy đúng giá đã chốt lúc đặt hàng (OrderItem.unitPrice), không lấy giá hiện tại của variant */
    public static ParcelItem from(OrderItem item) {
        ProductVariant variant = item.getVariant();
        return new ParcelItem(
                item.getProductName(),
                item.getVariantSku(),
                item.getQuantity(),
                variant.getWeight(),
                variant.getLength(),
                variant.getWidth(),
                variant.getHeight(),
                item.getUnitPrice()
        );
    }
}
