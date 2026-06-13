package com.fernirx.sneakerapi.product.repository;

import com.fernirx.sneakerapi.product.entity.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {

    List<ProductImage> findByProductIdOrderByPrimaryImageDescDisplayOrderAsc(Long productId);

    List<ProductImage> findByProductIdInOrderByPrimaryImageDescDisplayOrderAsc(List<Long> productIds);

    List<ProductImage> findByProductIdInAndPrimaryImageTrueOrderByProductIdAscDisplayOrderAsc(List<Long> productIds);

    List<ProductImage> findByProductIdOrderByColorwayAscDisplayOrderAsc(Long productId);

    Optional<ProductImage> findByIdAndProductId(Long id, Long productId);

    @Modifying
    @Query("UPDATE ProductImage pi SET pi.primaryImage = false WHERE pi.product.id = :productId AND pi.colorway = :colorway")
    void clearPrimaryByProductAndColorway(Long productId, String colorway);
}
