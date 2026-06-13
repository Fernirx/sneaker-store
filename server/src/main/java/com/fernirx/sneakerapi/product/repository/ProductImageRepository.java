package com.fernirx.sneakerapi.product.repository;

import com.fernirx.sneakerapi.product.entity.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {

    List<ProductImage> findByProductIdOrderByPrimaryImageDescDisplayOrderAsc(Long productId);

    List<ProductImage> findByProductIdInOrderByPrimaryImageDescDisplayOrderAsc(List<Long> productIds);
}
