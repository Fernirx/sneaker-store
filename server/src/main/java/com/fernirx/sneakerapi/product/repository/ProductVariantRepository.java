package com.fernirx.sneakerapi.product.repository;

import com.fernirx.sneakerapi.product.entity.ProductVariant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface ProductVariantRepository extends JpaRepository<ProductVariant, Long>, JpaSpecificationExecutor<ProductVariant> {

    List<ProductVariant> findByProductIdAndActiveTrueOrderByDisplayOrderAsc(Long productId);

    List<ProductVariant> findByProductIdInAndActiveTrueOrderByDisplayOrderAsc(List<Long> productIds);

    List<ProductVariant> findByProductIdOrderByColorwayAscSizeAsc(Long productId);

    Optional<ProductVariant> findByIdAndProductId(Long id, Long productId);

    boolean existsBySku(String sku);

    boolean existsBySkuAndIdNot(String sku, Long id);

    boolean existsByProductIdAndSizeAndColorwayAndShoeWidth(Long productId, Short size, String colorway, Object shoeWidth);
}
