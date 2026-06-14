package com.fernirx.sneakerapi.product.repository;

import com.fernirx.sneakerapi.product.dto.request.ProductFilterRequest;
import com.fernirx.sneakerapi.product.entity.Product;
import com.fernirx.sneakerapi.product.entity.ProductCategory;
import com.fernirx.sneakerapi.product.enums.Gender;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.List;

public class ProductSpec {

    public static Specification<Product> build(ProductFilterRequest filter) {
        return Specification
                .where(isActive())
                .and(hasKeyword(filter.search()))
                .and(hasGender(filter.gender()))
                .and(hasBrands(filter.brandSlugs()))
                .and(hasMinPrice(filter.minPrice()))
                .and(hasMaxPrice(filter.maxPrice()))
                .and(isNewArrival(filter.newArrival()))
                .and(isOnSale(filter.onSale()))
                .and(hasCategories(filter.categorySlugs()));
    }

    private static Specification<Product> isActive() {
        return (root, query, cb) -> cb.isTrue(root.get("active"));
    }

    private static Specification<Product> hasKeyword(String keyword) {
        return (root, query, cb) -> {
            if (!StringUtils.hasText(keyword)) return null;
            String pattern = "%" + keyword.toLowerCase() + "%";
            return cb.like(cb.lower(root.get("name")), pattern);
        };
    }

    private static Specification<Product> hasGender(Gender gender) {
        return (root, query, cb) -> gender == null ? null
                : cb.equal(root.get("gender"), gender);
    }

    private static Specification<Product> hasBrands(List<String> brandSlugs) {
        return (root, query, cb) -> {
            if (CollectionUtils.isEmpty(brandSlugs)) return null;
            Join<Object, Object> brandJoin = root.join("brand", JoinType.INNER);
            return brandJoin.get("slug").in(brandSlugs);
        };
    }

    private static Specification<Product> hasMinPrice(BigDecimal minPrice) {
        return (root, query, cb) -> minPrice == null ? null
                : cb.greaterThanOrEqualTo(root.get("basePrice"), minPrice);
    }

    private static Specification<Product> hasMaxPrice(BigDecimal maxPrice) {
        return (root, query, cb) -> maxPrice == null ? null
                : cb.lessThanOrEqualTo(root.get("basePrice"), maxPrice);
    }

    private static Specification<Product> isNewArrival(Boolean newArrival) {
        return (root, query, cb) -> newArrival == null ? null
                : cb.equal(root.get("newArrival"), newArrival);
    }

    private static Specification<Product> isOnSale(Boolean onSale) {
        return (root, query, cb) -> onSale == null ? null
                : cb.equal(root.get("onSale"), onSale);
    }

    private static Specification<Product> hasCategories(List<String> categorySlugs) {
        return (root, query, cb) -> {
            if (CollectionUtils.isEmpty(categorySlugs)) return null;
            Subquery<Long> sub = query.subquery(Long.class);
            Root<ProductCategory> pc = sub.from(ProductCategory.class);
            sub.select(pc.get("product").get("id"))
               .where(pc.get("category").get("slug").in(categorySlugs));
            return root.get("id").in(sub);
        };
    }
}
