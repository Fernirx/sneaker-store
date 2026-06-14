package com.fernirx.sneakerapi.product.repository;

import com.fernirx.sneakerapi.product.dto.request.InternalProductFilterRequest;
import com.fernirx.sneakerapi.product.entity.Product;
import com.fernirx.sneakerapi.product.enums.Gender;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

public class InternalProductSpec {

    public static Specification<Product> build(InternalProductFilterRequest filter) {
        return Specification
                .where(hasKeyword(filter.search()))
                .and(hasBrand(filter.brandId()))
                .and(hasGender(filter.gender()))
                .and(isActive(filter.active()))
                .and(isNewArrival(filter.newArrival()))
                .and(isOnSale(filter.onSale()));
    }

    private static Specification<Product> hasKeyword(String keyword) {
        return (root, query, cb) -> {
            if (!StringUtils.hasText(keyword)) return null;
            String pattern = "%" + keyword.toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("name")), pattern),
                    cb.like(cb.lower(root.get("code")), pattern)
            );
        };
    }

    private static Specification<Product> hasBrand(Long brandId) {
        return (root, query, cb) -> brandId == null ? null
                : cb.equal(root.get("brand").get("id"), brandId);
    }

    private static Specification<Product> hasGender(Gender gender) {
        return (root, query, cb) -> gender == null ? null
                : cb.equal(root.get("gender"), gender);
    }

    private static Specification<Product> isActive(Boolean active) {
        return (root, query, cb) -> active == null ? null
                : cb.equal(root.get("active"), active);
    }

    private static Specification<Product> isNewArrival(Boolean newArrival) {
        return (root, query, cb) -> newArrival == null ? null
                : cb.equal(root.get("newArrival"), newArrival);
    }

    private static Specification<Product> isOnSale(Boolean onSale) {
        return (root, query, cb) -> onSale == null ? null
                : cb.equal(root.get("onSale"), onSale);
    }
}
