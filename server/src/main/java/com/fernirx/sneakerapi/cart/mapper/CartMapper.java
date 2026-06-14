package com.fernirx.sneakerapi.cart.mapper;

import com.fernirx.sneakerapi.cart.dto.response.CartItemResponse;
import com.fernirx.sneakerapi.cart.entity.CartItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

import java.math.BigDecimal;

@Mapper(
        unmappedTargetPolicy = ReportingPolicy.ERROR,
        componentModel = MappingConstants.ComponentModel.SPRING
)
public interface CartMapper {

    @Mapping(target = "id", source = "item.id")
    @Mapping(target = "variantId", source = "item.variant.id")
    @Mapping(target = "quantity", source = "item.quantity")
    @Mapping(target = "productName", source = "item.variant.product.name")
    @Mapping(target = "productSlug", source = "item.variant.product.slug")
    @Mapping(target = "primaryImagePublicId", source = "primaryImagePublicId")
    @Mapping(target = "colorway", source = "item.variant.colorway")
    @Mapping(target = "size", source = "item.variant.size")
    @Mapping(target = "stockQuantity", source = "item.variant.stockQuantity")
    @Mapping(target = "outOfStock", expression = "java(resolveOutOfStock(item))")
    @Mapping(target = "unitPrice", expression = "java(resolveUnitPrice(item))")
    CartItemResponse toItemResponse(CartItem item, String primaryImagePublicId);

    default boolean resolveOutOfStock(CartItem item) {
        return !item.getVariant().getActive()
                || item.getVariant().getStockQuantity() == 0
                || !item.getVariant().getProduct().getActive();
    }

    default BigDecimal resolveUnitPrice(CartItem item) {
        BigDecimal variantPrice = item.getVariant().getPrice();
        return variantPrice != null ? variantPrice : item.getVariant().getProduct().getBasePrice();
    }
}
