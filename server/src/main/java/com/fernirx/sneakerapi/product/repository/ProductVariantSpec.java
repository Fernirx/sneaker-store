package com.fernirx.sneakerapi.product.repository;

import com.fernirx.sneakerapi.product.entity.ProductVariant;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

public class ProductVariantSpec {

    public static Specification<ProductVariant> build(String keyword) {
        return Specification.where(hasKeyword(keyword));
    }

    private static Specification<ProductVariant> hasKeyword(String keyword) {
        return (root, query, cb) -> {
            if (!StringUtils.hasText(keyword)) return null;
            String pattern = "%" + keyword.toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("sku")), pattern),
                    cb.like(cb.lower(root.get("colorway")), pattern),
                    cb.like(cb.lower(root.join("product").get("name")), pattern),
                    cb.like(cb.lower(root.join("product").get("code")), pattern)
            );
        };
    }
}
