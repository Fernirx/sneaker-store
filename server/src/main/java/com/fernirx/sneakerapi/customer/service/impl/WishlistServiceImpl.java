package com.fernirx.sneakerapi.customer.service.impl;

import com.fernirx.sneakerapi.common.exception.BusinessException;
import com.fernirx.sneakerapi.common.exception.SecurityCustomException;
import com.fernirx.sneakerapi.customer.dto.request.CreateWishlistRequest;
import com.fernirx.sneakerapi.customer.dto.response.WishlistResponse;
import com.fernirx.sneakerapi.customer.entity.Customer;
import com.fernirx.sneakerapi.customer.entity.Wishlist;
import com.fernirx.sneakerapi.customer.mapper.WishlistMapper;
import com.fernirx.sneakerapi.customer.repository.CustomerRepository;
import com.fernirx.sneakerapi.customer.repository.WishlistRepository;
import com.fernirx.sneakerapi.customer.service.WishlistService;
import com.fernirx.sneakerapi.product.entity.Product;
import com.fernirx.sneakerapi.product.entity.ProductVariant;
import com.fernirx.sneakerapi.product.service.ProductImageService;
import com.fernirx.sneakerapi.product.service.ProductService;
import com.fernirx.sneakerapi.product.service.ProductVariantService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@Transactional
@RequiredArgsConstructor
public class WishlistServiceImpl implements WishlistService {

    private final WishlistRepository wishlistRepository;
    private final CustomerRepository customerRepository;
    private final WishlistMapper wishlistMapper;
    private final ProductService productService;
    private final ProductVariantService productVariantService;
    private final ProductImageService productImageService;

    /**
     * Lấy danh sách sản phẩm yêu thích của khách hàng.
     * Hỗ trợ phân trang và tự động truy vấn kèm hình ảnh đại diện của từng sản phẩm.
     */
    @Override
    @Transactional(readOnly = true)
    public Page<WishlistResponse> getWishlist(Long userId, Pageable pageable) {
        Customer customer = findCustomer(userId);
        Page<Wishlist> page = wishlistRepository.findByCustomerId(customer.getId(), pageable);

        List<Long> productIds = page.getContent().stream()
                .map(w -> w.getProduct().getId())
                .distinct()
                .toList();
        Map<String, String> primaryImages = productImageService.getPrimaryImageMap(productIds);

        return page.map(w -> wishlistMapper.toResponse(w, resolveImage(w, primaryImages)));
    }

    /**
     * Thêm một sản phẩm (hoặc một phiên bản sản phẩm cụ thể) vào danh sách yêu thích.
     * Luồng xử lý:
     * 1. Xác thực Sản phẩm (và Variant nếu có) tồn tại.
     * 2. Nếu có Variant, đảm bảo Variant đó đúng là của Sản phẩm này.
     * 3. Kiểm tra chống trùng lặp: Nếu khách đã thêm đúng Sản phẩm/Variant này rồi thì văng lỗi.
     * 4. Lưu vào DB và trả về kết quả kèm hình ảnh.
     */
    @Override
    public WishlistResponse addWishlist(Long userId, CreateWishlistRequest request) {
        Customer customer = findCustomer(userId);
        Product product = productService.findEntityById(request.productId());

        ProductVariant variant = null;
        if (request.variantId() != null) {
            variant = productVariantService.findActiveById(request.variantId());
            if (!variant.getProduct().getId().equals(product.getId())) {
                throw BusinessException.bad("label.product.variant");
            }
        }

        boolean exists = variant != null
                ? wishlistRepository.existsByCustomer_IdAndProduct_IdAndVariant_Id(customer.getId(), product.getId(), variant.getId())
                : wishlistRepository.existsByCustomer_IdAndProduct_IdAndVariantIsNull(customer.getId(), product.getId());
        if (exists) {
            throw BusinessException.alreadyExists("label.wishlist");
        }

        Wishlist wishlist = new Wishlist();
        wishlist.setCustomer(customer);
        wishlist.setProduct(product);
        wishlist.setVariant(variant);
        wishlist = wishlistRepository.save(wishlist);

        Map<String, String> primaryImages = productImageService.getPrimaryImageMap(List.of(product.getId()));
        return wishlistMapper.toResponse(wishlist, resolveImage(wishlist, primaryImages));
    }

    /**
     * Xóa một sản phẩm khỏi danh sách yêu thích.
     * Luồng xử lý: Xác thực IDOR để đảm bảo chỉ chính khách hàng đó mới có quyền xóa.
     */
    @Override
    public void removeWishlist(Long userId, Long wishlistId) {
        Customer customer = findCustomer(userId);
        Wishlist wishlist = wishlistRepository.findById(wishlistId)
                .orElseThrow(() -> BusinessException.notFound("label.wishlist"));
        if (!wishlist.getCustomer().getId().equals(customer.getId())) {
            throw SecurityCustomException.forbidden();
        }
        wishlistRepository.delete(wishlist);
    }

    /**
     * Helper tìm Customer từ User ID.
     */
    private Customer findCustomer(Long userId) {
        return customerRepository.findByUserId(userId)
                .orElseThrow(() -> BusinessException.notFound("label.customer"));
    }

    /**
     * Helper lấy URL hình ảnh đại diện của sản phẩm dựa trên Colorway của Variant.
     */
    private String resolveImage(Wishlist wishlist, Map<String, String> primaryImages) {
        if (wishlist.getVariant() == null) return null;
        String key = wishlist.getProduct().getId() + ":" + wishlist.getVariant().getColorway();
        return primaryImages.get(key);
    }
}
