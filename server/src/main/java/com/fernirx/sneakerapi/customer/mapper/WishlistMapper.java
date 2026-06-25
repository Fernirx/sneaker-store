package com.fernirx.sneakerapi.customer.mapper;

import com.fernirx.sneakerapi.customer.dto.response.WishlistResponse;
import com.fernirx.sneakerapi.customer.entity.Wishlist;
import com.fernirx.sneakerapi.product.entity.Product;
import com.fernirx.sneakerapi.product.entity.ProductVariant;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

import java.math.BigDecimal;

@Mapper(
        unmappedTargetPolicy = ReportingPolicy.ERROR,
        componentModel = MappingConstants.ComponentModel.SPRING
)
public interface WishlistMapper {

    @Mapping(target = "id", source = "wishlist.id")
    @Mapping(target = "productId", source = "wishlist.product.id")
    @Mapping(target = "productName", source = "wishlist.product.name")
    @Mapping(target = "productSlug", source = "wishlist.product.slug")
    @Mapping(target = "brandName", source = "wishlist.product.brand.name")
    @Mapping(target = "primaryImagePublicId", source = "primaryImagePublicId")
    @Mapping(target = "variantId", source = "wishlist.variant.id")
    @Mapping(target = "colorway", source = "wishlist.variant.colorway")
    @Mapping(target = "colorHex", source = "wishlist.variant.colorHex")
    @Mapping(target = "size", source = "wishlist.variant.size")
    @Mapping(target = "createdAt", source = "wishlist.createdAt")
    @Mapping(target = "price", expression = "java(resolvePrice(wishlist))")
    @Mapping(target = "originalPrice", expression = "java(resolveOriginalPrice(wishlist))")
    @Mapping(target = "outOfStock", expression = "java(resolveOutOfStock(wishlist))")
    WishlistResponse toResponse(Wishlist wishlist, String primaryImagePublicId);

    default BigDecimal resolvePrice(Wishlist wishlist) {
        ProductVariant variant = wishlist.getVariant();
        Product product = wishlist.getProduct();
        return variant != null && variant.getPrice() != null ? variant.getPrice() : product.getBasePrice();
    }

    default BigDecimal resolveOriginalPrice(Wishlist wishlist) {
        BigDecimal originalPrice = wishlist.getProduct().getOriginalPrice();
        if (originalPrice == null) return null;
        BigDecimal price = resolvePrice(wishlist);
        return originalPrice.compareTo(price) > 0 ? originalPrice : null;
    }

    default boolean resolveOutOfStock(Wishlist wishlist) {
        Product product = wishlist.getProduct();
        ProductVariant variant = wishlist.getVariant();
        if (variant != null) {
            return !variant.getActive() || variant.getStockQuantity() == 0 || !product.getActive();
        }
        return !product.getActive();
    }
}
