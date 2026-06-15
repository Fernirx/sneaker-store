package com.fernirx.sneakerapi.cart.repository;

import com.fernirx.sneakerapi.cart.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {

    Optional<Cart> findByCustomer_User_Id(Long userId);

    Optional<Cart> findByGuestToken(String guestToken);
}