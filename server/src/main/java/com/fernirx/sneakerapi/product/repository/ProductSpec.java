package com.fernirx.sneakerapi.product.repository;

import com.fernirx.sneakerapi.product.dto.request.ProductFilterRequest;
import com.fernirx.sneakerapi.product.entity.Product;
import com.fernirx.sneakerapi.product.entity.ProductCategory;
import com.fernirx.sneakerapi.product.entity.ProductCollection;
import com.fernirx.sneakerapi.product.entity.ProductVariant;
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
                .and(hasCategories(filter.categorySlugs()))
                .and(hasCollections(filter.collectionSlugs()))
                .and(hasSizes(filter.sizes()));
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
        return (root, query, cb) -> {
            if (gender == null) return null;
            if (gender == Gender.MEN || gender == Gender.WOMEN) {
                return root.get("gender").in(gender, Gender.UNISEX);
            }
            return cb.equal(root.get("gender"), gender);
        };
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
                : cb.greaterThanOrEqualTo(root.get("maxPrice"), minPrice);
    }

    private static Specification<Product> hasMaxPrice(BigDecimal maxPrice) {
        return (root, query, cb) -> maxPrice == null ? null
                : cb.lessThanOrEqualTo(root.get("minPrice"), maxPrice);
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

    private static Specification<Product> hasCollections(List<String> collectionSlugs) {
        return (root, query, cb) -> {
            if (CollectionUtils.isEmpty(collectionSlugs)) return null;
            Subquery<Long> sub = query.subquery(Long.class);
            Root<ProductCollection> pc = sub.from(ProductCollection.class);
            sub.select(pc.get("product").get("id"))
               .where(pc.get("collection").get("slug").in(collectionSlugs));
            return root.get("id").in(sub);
        };
    }

    private static Specification<Product> hasSizes(List<Short> sizes) {
        return (root, query, cb) -> {
            if (CollectionUtils.isEmpty(sizes)) return null;
            Subquery<Long> sub = query.subquery(Long.class);
            Root<ProductVariant> pv = sub.from(ProductVariant.class);
            sub.select(pv.get("product").get("id"))
               .where(pv.get("size").in(sizes));
            return root.get("id").in(sub);
        };
    }
}
