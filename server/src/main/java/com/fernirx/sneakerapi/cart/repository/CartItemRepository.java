package com.fernirx.sneakerapi.cart.repository;

import com.fernirx.sneakerapi.cart.entity.Cart;
import com.fernirx.sneakerapi.cart.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    @Query("SELECT ci FROM CartItem ci JOIN FETCH ci.variant v JOIN FETCH v.product WHERE ci.cart = :cart ORDER BY ci.createdAt ASC")
    List<CartItem> findAllWithDetailsBy(@Param("cart") Cart cart);

    Optional<CartItem> findByIdAndCart(Long id, Cart cart);

    Optional<CartItem> findByCartAndVariant_Id(Cart cart, Long variantId);

    @Modifying
    @Query("DELETE FROM CartItem ci WHERE ci.cart = :cart")
    void deleteAllByCart(@Param("cart") Cart cart);

    @Modifying
    @Query("DELETE FROM CartItem ci WHERE ci.cart = :cart AND ci.selected = true")
    void deleteSelectedByCart(@Param("cart") Cart cart);
}
