package com.fernirx.sneakerapi.customer.service;

import com.fernirx.sneakerapi.customer.dto.request.CreateWishlistRequest;
import com.fernirx.sneakerapi.customer.dto.response.WishlistResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface WishlistService {
    Page<WishlistResponse> getWishlist(Long userId, Pageable pageable);
    WishlistResponse addWishlist(Long userId, CreateWishlistRequest request);
    void removeWishlist(Long userId, Long wishlistId);
}
