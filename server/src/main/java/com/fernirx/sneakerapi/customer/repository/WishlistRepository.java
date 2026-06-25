package com.fernirx.sneakerapi.customer.repository;

import com.fernirx.sneakerapi.customer.entity.Wishlist;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface WishlistRepository extends JpaRepository<Wishlist, Long> {

    @Query("SELECT w FROM Wishlist w JOIN FETCH w.product p JOIN FETCH p.brand LEFT JOIN FETCH w.variant " +
            "WHERE w.customer.id = :customerId")
    Page<Wishlist> findByCustomerId(@Param("customerId") Long customerId, Pageable pageable);

    boolean existsByCustomer_IdAndProduct_IdAndVariant_Id(Long customerId, Long productId, Long variantId);

    boolean existsByCustomer_IdAndProduct_IdAndVariantIsNull(Long customerId, Long productId);
}
