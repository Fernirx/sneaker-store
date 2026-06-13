package com.fernirx.sneakerapi.product.repository;

import com.fernirx.sneakerapi.product.entity.ProductVariant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface ProductVariantRepository extends JpaRepository<ProductVariant, Long>, JpaSpecificationExecutor<ProductVariant> {

    List<ProductVariant> findByProductIdAndActiveTrueOrderByDisplayOrderAsc(Long productId);

    List<ProductVariant> findByProductIdInAndActiveTrueOrderByDisplayOrderAsc(List<Long> productIds);
}
