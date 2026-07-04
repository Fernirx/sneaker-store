package com.fernirx.sneakerapi.product.repository;

import com.fernirx.sneakerapi.product.entity.ProductVariant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductVariantRepository extends JpaRepository<ProductVariant, Long>, JpaSpecificationExecutor<ProductVariant> {

    List<ProductVariant> findByProductIdAndActiveTrueOrderByDisplayOrderAsc(Long productId);

    List<ProductVariant> findByProductIdInAndActiveTrueOrderByDisplayOrderAsc(List<Long> productIds);

    List<ProductVariant> findByIdInAndActiveTrue(List<Long> ids);

    List<ProductVariant> findByProductIdOrderByColorwayAscSizeAsc(Long productId);

    Optional<ProductVariant> findByIdAndProductId(Long id, Long productId);

    boolean existsBySku(String sku);

    boolean existsBySkuAndIdNot(String sku, Long id);

    boolean existsByProductIdAndActiveTrue(Long productId);

    boolean existsByProductIdAndSizeAndColorwayAndShoeWidth(Long productId, Short size, String colorway, Object shoeWidth);

    @Query("SELECT v.stockQuantity FROM ProductVariant v WHERE v.id = :id")
    Optional<Integer> findStockQuantityById(@Param("id") Long id);

    @Modifying
    @Query("UPDATE ProductVariant v SET v.stockQuantity = v.stockQuantity - :quantity " +
            "WHERE v.id = :id AND v.stockQuantity >= :quantity")
    int decreaseStockAtomic(@Param("id") Long id, @Param("quantity") int quantity);

    @Modifying
    @Query("UPDATE ProductVariant v SET v.stockQuantity = v.stockQuantity + :quantity WHERE v.id = :id")
    int increaseStockAtomic(@Param("id") Long id, @Param("quantity") int quantity);
}
