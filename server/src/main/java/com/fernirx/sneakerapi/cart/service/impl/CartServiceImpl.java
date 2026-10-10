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
import com.fernirx.sneakerapi.customer.enums.MembershipTier;
import com.fernirx.sneakerapi.product.entity.ProductVariant;
import com.fernirx.sneakerapi.product.service.ProductImageService;
import com.fernirx.sneakerapi.product.service.ProductVariantService;
import com.fernirx.sneakerapi.setting.dto.response.StoreSettingResponse;
import com.fernirx.sneakerapi.setting.service.SettingService;
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
    private final SettingService settingService;
    private final CartMapper cartMapper;

    /**
     * Lấy thông tin giỏ hàng hiện tại.
     * Luồng xử lý:
     * 1. Phân giải (resolve) giỏ hàng dựa trên userId hoặc guestToken.
     * 2. Nếu có giỏ hàng, gọi hàm buildCartResponse để build dữ liệu trả về 
     *    (đồng thời tự động cắt giảm số lượng nếu hàng trong kho bị hụt).
     * 3. Nếu chưa có, trả về giỏ hàng rỗng.
     */
    @Override
    public CartResponse getCart(Long userId, String guestToken) {
        return resolveCart(userId, guestToken)
                .map(this::buildCartResponse) // Ánh xạ Cart thành CartResponse kèm chi tiết
                .orElseGet(this::emptyCartResponse);
    }

    /**
     * Thêm một sản phẩm (variant) vào giỏ hàng.
     * Luồng xử lý:
     * 1. Kiểm tra variant có đang hiển thị (active) không.
     * 2. Tìm hoặc tạo mới giỏ hàng cho user/guest.
     * 3. Tìm xem variant này đã có trong giỏ chưa.
     *    - Nếu có: Cộng dồn số lượng. Validate tồn kho với tổng số lượng mới.
     *    - Nếu chưa: Validate tồn kho. Tạo mới CartItem.
     * 4. Build và trả về giỏ hàng mới nhất.
     */
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

    /**
     * Cập nhật số lượng của một sản phẩm trong giỏ hàng.
     * Luồng xử lý:
     * 1. Tìm giỏ hàng theo userId/guestToken.
     * 2. Tìm CartItem theo itemId và phải thuộc đúng giỏ hàng này (Security check).
     * 3. Validate lại số lượng tồn kho xem có đủ cho quantity mới không.
     * 4. Cập nhật số lượng và trả về giỏ.
     */
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

    /**
     * Xóa hẳn một sản phẩm khỏi giỏ hàng.
     */
    @Override
    public CartResponse removeItem(Long userId, String guestToken, Long itemId) {
        Cart cart = resolveCart(userId, guestToken)
                .orElseThrow(() -> BusinessException.notFound("label.cart"));
        CartItem item = cartItemRepository.findByIdAndCart(itemId, cart)
                .orElseThrow(() -> BusinessException.notFound("label.cart.item"));

        cartItemRepository.delete(item);
        return buildCartResponse(cart);
    }

    /**
     * Xóa sạch toàn bộ sản phẩm trong giỏ hàng.
     */
    @Override
    public CartResponse clearCart(Long userId, String guestToken) {
        Cart cart = resolveCart(userId, guestToken)
                .orElseThrow(() -> BusinessException.notFound("label.cart"));
        cartItemRepository.deleteAllByCart(cart);
        return emptyCartResponse(cart.getGuestToken());
    }

    /**
     * Đánh dấu chọn / bỏ chọn một sản phẩm (để chuẩn bị thanh toán).
     */
    @Override
    public CartResponse selectItem(
            Long userId, String guestToken, Long itemId, UpdateCartItemSelectionRequest request) {
        Cart cart = resolveCart(userId, guestToken)
                .orElseThrow(() -> BusinessException.notFound("label.cart"));
        CartItem item = cartItemRepository.findByIdAndCart(itemId, cart)
                .orElseThrow(() -> BusinessException.notFound("label.cart.item"));
        item.setSelected(request.selected());
        return buildCartResponse(cart);
    }

    /**
     * Mua ngay: Hủy chọn tất cả sản phẩm khác trong giỏ, 
     * thêm sản phẩm mới (hoặc cộng dồn) và chỉ chọn duy nhất sản phẩm này.
     */
    @Override
    public CartResponse buyNow(Long userId, String guestToken, AddCartItemRequest request) {
        ProductVariant variant = findActiveVariant(request.variantId());
        Cart cart = getOrCreateCart(userId, guestToken);

        // Lấy tất cả item hiện tại trong giỏ và bỏ chọn
        List<CartItem> items = cartItemRepository.findAllWithDetailsBy(cart);
        items.forEach(item -> item.setSelected(false));

        // Tìm xem sản phẩm Mua Ngay đã có trong giỏ chưa
        Optional<CartItem> existingItemOpt = items.stream()
                .filter(i -> i.getVariant().getId().equals(variant.getId()))
                .findFirst();

        if (existingItemOpt.isPresent()) {
            CartItem item = existingItemOpt.get();
            int newQty = item.getQuantity() + request.quantity();
            validateStock(variant, newQty);
            item.setQuantity(newQty);
            item.setSelected(true); // Chỉ chọn món này
        } else {
            validateStock(variant, request.quantity());
            CartItem newItem = new CartItem();
            newItem.setCart(cart);
            newItem.setVariant(variant);
            newItem.setQuantity(request.quantity());
            newItem.setSelected(true); // Chỉ chọn món này
            cartItemRepository.save(newItem);
        }
        
        cartItemRepository.saveAll(items);

        return buildCartResponse(cart);
    }

    /**
     * Đồng bộ (gộp) giỏ hàng vãng lai (Guest) vào giỏ hàng tài khoản (User).
     * Luồng xử lý:
     * 1. Lấy giỏ hàng Guest và giỏ hàng User (nếu chưa có thì tạo mới).
     * 2. Lặp qua các món trong giỏ Guest:
     *    - Nếu món đó đã có trong giỏ User: Cộng dồn số lượng nhưng tự động cắt 
     *      giảm để không vượt quá tồn kho (Math.min).
     *    - Nếu chưa có: Tạo copy món hàng đưa sang giỏ User (cũng giới hạn tồn kho).
     *    - Lưu ý: Nếu hàng đã hết (stock=0), giữ nguyên số lượng cũ để FE hiển thị 
     *      cảnh báo hết hàng.
     * 3. Xóa hoàn toàn giỏ hàng Guest (nhờ Cascade sẽ xóa luôn các CartItem cũ).
     */
    @Override
    public CartResponse mergeGuestCart(Long userId, String guestToken) {
        Optional<Cart> guestCartOpt = cartRepository.findByGuestToken(guestToken);
        Cart userCart = getOrCreateUserCart(userId);

        guestCartOpt.ifPresent(guestCart -> {
            List<CartItem> guestItems = cartItemRepository.findAllWithDetailsBy(guestCart);

            guestItems.forEach(guestItem -> {
                ProductVariant variant = guestItem.getVariant();
                int stock = variant.getStockQuantity();
                
                // Tìm xem món hàng này đã có trong giỏ của user chưa
                cartItemRepository.findByCartAndVariant_Id(userCart, variant.getId())
                        .ifPresentOrElse(
                                existing -> {
                                    // Đã có trong giỏ user -> Cộng dồn số lượng
                                    if (stock > 0) {
                                        int merged = existing.getQuantity() + guestItem.getQuantity();
                                        existing.setQuantity(Math.min(merged, stock));
                                    }
                                    // Nếu stock = 0: cố tình không cập nhật để giữ nguyên quantity, 
                                    // qua đó báo hiệu cho FE hiển thị nhãn outOfStock
                                },
                                () -> {
                                    // Chưa có trong giỏ user -> Thêm mới bản copy
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

            // Xóa giỏ hàng guest sau khi merge xong
            cartRepository.delete(guestCart);
        });

        return buildCartResponse(userCart);
    }

    /**
     * Xóa các sản phẩm đã được đánh dấu chọn (dùng sau khi đặt hàng thành công).
     */
    @Override
    public void clearSelectedItems(Long userId, String guestToken) {
        resolveCart(userId, guestToken).ifPresent(cartItemRepository::deleteSelectedByCart);
    }

    // ---- Private helpers ----

    /**
     * Ưu tiên tìm giỏ hàng theo userId nếu đã đăng nhập, 
     * ngược lại tìm theo guestToken.
     * Trả về Optional rỗng nếu không tìm thấy.
     */
    private Optional<Cart> resolveCart(Long userId, String guestToken) {
        if (userId != null) return cartRepository.findByCustomer_User_Id(userId);
        if (guestToken != null) return cartRepository.findByGuestToken(guestToken);
        return Optional.empty();
    }

    /**
     * Lấy giỏ hàng hiện tại hoặc tạo mới nếu chưa có.
     * Luồng xử lý:
     * 1. Cố gắng tìm giỏ hàng cũ.
     * 2. Nếu không thấy, tạo mới và gắn Customer (nếu có userId) 
     *    hoặc sinh UUID ngẫu nhiên (nếu là khách vãng lai).
     */
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

    /**
     * Dành riêng cho User: Lấy giỏ hàng hoặc tạo mới dựa trên userId.
     */
    private Cart getOrCreateUserCart(Long userId) {
        return cartRepository.findByCustomer_User_Id(userId).orElseGet(() -> {
            Cart cart = new Cart();
            cart.setCustomer(findOrCreateCustomer(userId));
            return cartRepository.save(cart);
        });
    }

    /**
     * Tìm thông tin Customer theo userId, nếu chưa có profile thì tạo mới.
     */
    private Customer findOrCreateCustomer(Long userId) {
        return customerService.getOrCreateByUserId(userId);
    }

    /**
     * Tìm ProductVariant theo ID và đảm bảo Variant cùng Product cha đều đang hiển thị (active).
     * Ném lỗi 404 nếu không hợp lệ.
     */
    private ProductVariant findActiveVariant(Long variantId) {
        return productVariantService.findActiveById(variantId);
    }

    /**
     * Kiểm tra nhanh số lượng tồn kho so với số lượng yêu cầu.
     * Ném lỗi nghiệp vụ nếu vượt quá tồn kho.
     */
    private void validateStock(ProductVariant variant, int requestedQty) {
        if (variant.getStockQuantity() < requestedQty) {
            throw BusinessException.bad("label.cart.quantity");
        }
    }

    /**
     * Dựng Response chi tiết cho giỏ hàng. Kèm tính năng tự động điều chỉnh.
     * Luồng xử lý:
     * 1. Query toàn bộ CartItem (kèm Variant, Product).
     * 2. Quét mảng để phát hiện món nào có số lượng yêu cầu > tồn kho hiện tại.
     * 3. Tự động ép quantity xuống bằng tồn kho và save lại DB.
     * 4. Lấy ảnh chính (primaryImage) của các sản phẩm để trả về cho FE.
     * 5. Tính toán tổng tiền (chỉ tính các item đã được selected).
     */
    private CartResponse buildCartResponse(Cart cart) {
        List<CartItem> items = cartItemRepository.findAllWithDetailsBy(cart);

        if (items.isEmpty()) {
            return emptyCartResponse(cart.getGuestToken());
        }

        // Lưu lại số lượng trước khi bị auto-adjust để FE biết báo lỗi hụt hàng
        Map<Long, Integer> previousQties = items.stream()
                .collect(Collectors.toMap(CartItem::getId, CartItem::getQuantity));

        // Lọc ra các món bị lố tồn kho (stock > 0 nhưng giỏ hàng > stock)
        // và tự động gọt bớt quantity xuống bằng tồn kho hiện tại.
        List<CartItem> toUpdate = items.stream()
                .filter(item -> {
                    int stock = item.getVariant().getStockQuantity();
                    return stock > 0 && item.getQuantity() > stock;
                })
                .peek(item -> item.setQuantity(item.getVariant().getStockQuantity()))
                .toList();
                
        // Lưu các món vừa bị gọt số lượng xuống DB
        if (!toUpdate.isEmpty()) {
            cartItemRepository.saveAll(toUpdate);
        }

        // Lấy danh sách ID của các món bị ép số lượng để map vào Response
        Set<Long> adjustedIds = toUpdate.stream()
                .map(CartItem::getId)
                .collect(Collectors.toSet());

        // Thu thập danh sách productId (không trùng) để lấy ảnh đại diện
        List<Long> productIds = items.stream()
                .map(ci -> ci.getVariant().getProduct().getId())
                .distinct()
                .toList();

        // Query ảnh đại diện 1 lần (Map key: productId:colorway)
        Map<String, String> primaryImages = productImageService.getPrimaryImageMap(productIds);

        // Build danh sách CartItemResponse
        List<CartItemResponse> itemResponses = items.stream()
                .map(item -> {
                    String key = item.getVariant().getProduct().getId() + ":" 
                            + item.getVariant().getColorway();
                    
                    // Nếu item này vừa bị auto-adjust, truyền quantity cũ vào response
                    Integer previousQuantity = adjustedIds.contains(item.getId())
                            ? previousQties.get(item.getId()) : null;
                            
                    return cartMapper.toItemResponse(item, primaryImages.get(key), previousQuantity);
                })
                .toList();

        BigDecimal totalAmount = itemResponses.stream()
                .filter(r -> Boolean.TRUE.equals(r.selected()))
                .map(r -> r.unitPrice().multiply(BigDecimal.valueOf(r.quantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal tierDiscountAmount = BigDecimal.ZERO;
        Integer tierDiscountRate = 0;

        String tierName = null;
        if (cart.getCustomer() != null && cart.getCustomer().getMembershipTier() != MembershipTier.BRONZE) {
            StoreSettingResponse setting = settingService.getStoreSetting();
            tierName = switch (cart.getCustomer().getMembershipTier()) {
                case SILVER -> "Silver";
                case GOLD -> "Gold";
                case PLATINUM -> "Platinum";
                default -> null;
            };
            tierDiscountRate = switch (cart.getCustomer().getMembershipTier()) {
                case SILVER -> setting.silverDiscountRate();
                case GOLD -> setting.goldDiscountRate();
                case PLATINUM -> setting.platinumDiscountRate();
                default -> 0;
            };
            if (tierDiscountRate != null && tierDiscountRate > 0) {
                tierDiscountAmount = totalAmount.multiply(BigDecimal.valueOf(tierDiscountRate)).divide(BigDecimal.valueOf(100));
            } else {
                tierDiscountRate = 0;
            }
        }

        return new CartResponse(cart.getGuestToken(), itemResponses, items.size(), totalAmount, tierDiscountAmount, tierDiscountRate, tierName);
    }

    /**
     * Hàm phụ trợ trả về một Response giỏ hàng rỗng (khi giỏ hàng chưa có).
     */
    private CartResponse emptyCartResponse() {
        return new CartResponse(null, List.of(), 0, BigDecimal.ZERO, BigDecimal.ZERO, 0, null);
    }

    /**
     * Hàm phụ trợ trả về một Response giỏ hàng rỗng nhưng kèm theo guestToken (để FE lưu Cookie).
     */
    private CartResponse emptyCartResponse(String guestToken) {
        return new CartResponse(guestToken, List.of(), 0, BigDecimal.ZERO, BigDecimal.ZERO, 0, null);
    }
}
