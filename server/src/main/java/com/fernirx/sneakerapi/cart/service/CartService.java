package com.fernirx.sneakerapi.cart.service;

import com.fernirx.sneakerapi.cart.dto.request.AddCartItemRequest;
import com.fernirx.sneakerapi.cart.dto.request.UpdateCartItemRequest;
import com.fernirx.sneakerapi.cart.dto.response.CartResponse;

public interface CartService {
    CartResponse getCart(Long userId, String guestToken);
    CartResponse addItem(Long userId, String guestToken, AddCartItemRequest request);
    CartResponse updateItem(Long userId, String guestToken, Long itemId, UpdateCartItemRequest request);
    CartResponse removeItem(Long userId, String guestToken, Long itemId);
    CartResponse clearCart(Long userId, String guestToken);
    CartResponse mergeGuestCart(Long userId, String guestToken);
}
