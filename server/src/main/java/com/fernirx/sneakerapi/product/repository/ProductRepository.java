package com.fernirx.sneakerapi.product.repository;

import com.fernirx.sneakerapi.product.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {

    @Query("SELECT p FROM Product p JOIN FETCH p.brand WHERE p.id IN :ids")
    List<Product> findAllWithBrandByIds(List<Long> ids);

    @Query("SELECT p FROM Product p JOIN FETCH p.brand WHERE p.slug = :slug AND p.active = true")
    Optional<Product> findActiveBySlugWithBrand(String slug);

    @Modifying
    @Query("UPDATE Product p SET p.viewCount = p.viewCount + 1 WHERE p.id = :id")
    void incrementViewCount(Long id);
}
