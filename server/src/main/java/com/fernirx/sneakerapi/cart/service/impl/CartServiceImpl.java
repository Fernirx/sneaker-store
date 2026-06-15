package com.fernirx.sneakerapi.cart.service.impl;

import com.fernirx.sneakerapi.cart.dto.request.AddCartItemRequest;
import com.fernirx.sneakerapi.cart.dto.request.UpdateCartItemRequest;
import com.fernirx.sneakerapi.cart.dto.request.UpdateCartItemSelectionRequest;
import com.fernirx.sneakerapi.cart.dto.response.CartItemResponse;
import com.fernirx.sneakerapi.cart.dto.response.CartResponse;
import com.fernirx.sneakerapi.cart.entity.Cart;
import com.fernirx.sneakerapi.cart.entity.CartItem;
import com.fernirx.sneakerapi.cart.mapper.CartMapper;
import com.fernirx.sneakerapi.cart.repository.CartItemRepository;
import com.fernirx.sneakerapi.cart.repository.CartRepository;
import com.fernirx.sneakerapi.cart.service.CartService;
import com.fernirx.sneakerapi.common.exception.BusinessException;
import com.fernirx.sneakerapi.customer.entity.Customer;
import com.fernirx.sneakerapi.customer.service.CustomerService;
import com.fernirx.sneakerapi.product.entity.ProductVariant;
import com.fernirx.sneakerapi.product.service.ProductImageService;
import com.fernirx.sneakerapi.product.service.ProductVariantService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class CartServiceImpl implements CartService {
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductVariantService productVariantService;
    private final ProductImageService productImageService;
    private final CustomerService customerService;
    private final CartMapper cartMapper;

    @Override
    public CartResponse getCart(Long userId, String guestToken) {
        return resolveCart(userId, guestToken)
                .map(this::buildCartResponse)
                .orElseGet(this::emptyCartResponse);
    }

    @Override
    public CartResponse addItem(Long userId, String guestToken, AddCartItemRequest request) {
        ProductVariant variant = findActiveVariant(request.variantId());
        Cart cart = getOrCreateCart(userId, guestToken);

        Optional<CartItem> existingItem = cartItemRepository.findByCartAndVariant_Id(cart, variant.getId());

        if (existingItem.isPresent()) {
            CartItem item = existingItem.get();
            int newQty = item.getQuantity() + request.quantity();
            validateStock(variant, newQty);
            item.setQuantity(newQty);
        } else {
            validateStock(variant, request.quantity());
            CartItem newItem = new CartItem();
            newItem.setCart(cart);
            newItem.setVariant(variant);
            newItem.setQuantity(request.quantity());
            cartItemRepository.save(newItem);
        }

        return buildCartResponse(cart);
    }

    @Override
    public CartResponse updateItem(Long userId, String guestToken, Long itemId, UpdateCartItemRequest request) {
        Cart cart = resolveCart(userId, guestToken)
                .orElseThrow(() -> BusinessException.notFound("label.cart"));
        CartItem item = cartItemRepository.findByIdAndCart(itemId, cart)
                .orElseThrow(() -> BusinessException.notFound("label.cart.item"));

        validateStock(item.getVariant(), request.quantity());
        item.setQuantity(request.quantity());
        return buildCartResponse(cart);
    }

    @Override
    public CartResponse removeItem(Long userId, String guestToken, Long itemId) {
        Cart cart = resolveCart(userId, guestToken)
                .orElseThrow(() -> BusinessException.notFound("label.cart"));
        CartItem item = cartItemRepository.findByIdAndCart(itemId, cart)
                .orElseThrow(() -> BusinessException.notFound("label.cart.item"));

        cartItemRepository.delete(item);
        return buildCartResponse(cart);
    }

    @Override
    public CartResponse clearCart(Long userId, String guestToken) {
        Cart cart = resolveCart(userId, guestToken)
                .orElseThrow(() -> BusinessException.notFound("label.cart"));
        cartItemRepository.deleteAllByCart(cart);
        return emptyCartResponse(cart.getGuestToken());
    }

    @Override
    public CartResponse selectItem(Long userId, String guestToken, Long itemId, UpdateCartItemSelectionRequest request) {
        Cart cart = resolveCart(userId, guestToken)
                .orElseThrow(() -> BusinessException.notFound("label.cart"));
        CartItem item = cartItemRepository.findByIdAndCart(itemId, cart)
                .orElseThrow(() -> BusinessException.notFound("label.cart.item"));
        item.setSelected(request.selected());
        return buildCartResponse(cart);
    }

    @Override
    public CartResponse mergeGuestCart(Long userId, String guestToken) {
        Optional<Cart> guestCartOpt = cartRepository.findByGuestToken(guestToken);
        Cart userCart = getOrCreateUserCart(userId);

        guestCartOpt.ifPresent(guestCart -> {
            List<CartItem> guestItems = cartItemRepository.findAllWithDetailsBy(guestCart);

            guestItems.forEach(guestItem -> {
                ProductVariant variant = guestItem.getVariant();
                int stock = variant.getStockQuantity();
                cartItemRepository.findByCartAndVariant_Id(userCart, variant.getId())
                        .ifPresentOrElse(
                                existing -> {
                                    if (stock > 0) {
                                        int merged = existing.getQuantity() + guestItem.getQuantity();
                                        existing.setQuantity(Math.min(merged, stock));
                                    }
                                    // stock = 0: giữ nguyên qty, FE hiển thị outOfStock
                                },
                                () -> {
                                    CartItem copy = new CartItem();
                                    copy.setCart(userCart);
                                    copy.setVariant(variant);
                                    copy.setQuantity(stock > 0
                                            ? Math.min(guestItem.getQuantity(), stock)
                                            : guestItem.getQuantity());
                                    cartItemRepository.save(copy);
                                }
                        );
            });

            cartRepository.delete(guestCart);
        });

        return buildCartResponse(userCart);
    }

    // ---- Private helpers ----

    private Optional<Cart> resolveCart(Long userId, String guestToken) {
        if (userId != null) return cartRepository.findByCustomer_User_Id(userId);
        if (guestToken != null) return cartRepository.findByGuestToken(guestToken);
        return Optional.empty();
    }

    private Cart getOrCreateCart(Long userId, String guestToken) {
        return resolveCart(userId, guestToken).orElseGet(() -> {
            Cart cart = new Cart();
            if (userId != null) {
                cart.setCustomer(findOrCreateCustomer(userId));
            } else {
                cart.setGuestToken(UUID.randomUUID().toString());
            }
            return cartRepository.save(cart);
        });
    }

    private Cart getOrCreateUserCart(Long userId) {
        return cartRepository.findByCustomer_User_Id(userId).orElseGet(() -> {
            Cart cart = new Cart();
            cart.setCustomer(findOrCreateCustomer(userId));
            return cartRepository.save(cart);
        });
    }

    private Customer findOrCreateCustomer(Long userId) {
        return customerService.getOrCreateByUserId(userId);
    }

    private ProductVariant findActiveVariant(Long variantId) {
        return productVariantService.findActiveById(variantId);
    }

    private void validateStock(ProductVariant variant, int requestedQty) {
        if (variant.getStockQuantity() < requestedQty) {
            throw BusinessException.bad("label.cart.quantity");
        }
    }

    private CartResponse buildCartResponse(Cart cart) {
        List<CartItem> items = cartItemRepository.findAllWithDetailsBy(cart);

        if (items.isEmpty()) {
            return emptyCartResponse(cart.getGuestToken());
        }

        Map<Long, Integer> previousQties = items.stream()
                .collect(Collectors.toMap(CartItem::getId, CartItem::getQuantity));

        List<CartItem> toUpdate = items.stream()
                .filter(item -> {
                    int stock = item.getVariant().getStockQuantity();
                    return stock > 0 && item.getQuantity() > stock;
                })
                .peek(item -> item.setQuantity(item.getVariant().getStockQuantity()))
                .toList();
        if (!toUpdate.isEmpty()) {
            cartItemRepository.saveAll(toUpdate);
        }

        Set<Long> adjustedIds = toUpdate.stream()
                .map(CartItem::getId)
                .collect(Collectors.toSet());

        List<Long> productIds = items.stream()
                .map(ci -> ci.getVariant().getProduct().getId())
                .distinct()
                .toList();

        Map<String, String> primaryImages = productImageService.getPrimaryImageMap(productIds);

        List<CartItemResponse> itemResponses = items.stream()
                .map(item -> {
                    String key = item.getVariant().getProduct().getId() + ":" + item.getVariant().getColorway();
                    Integer previousQuantity = adjustedIds.contains(item.getId())
                            ? previousQties.get(item.getId()) : null;
                    return cartMapper.toItemResponse(item, primaryImages.get(key), previousQuantity);
                })
                .toList();

        BigDecimal totalAmount = itemResponses.stream()
                .filter(r -> Boolean.TRUE.equals(r.selected()))
                .map(r -> r.unitPrice().multiply(BigDecimal.valueOf(r.quantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new CartResponse(cart.getGuestToken(), itemResponses, items.size(), totalAmount);
    }

    private CartResponse emptyCartResponse() {
        return new CartResponse(null, Collections.emptyList(), 0, BigDecimal.ZERO);
    }

    private CartResponse emptyCartResponse(String guestToken) {
        return new CartResponse(guestToken, Collections.emptyList(), 0, BigDecimal.ZERO);
    }
}
