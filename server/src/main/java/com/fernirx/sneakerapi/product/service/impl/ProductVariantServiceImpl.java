package com.fernirx.sneakerapi.product.service.impl;

import com.fernirx.sneakerapi.common.enums.ErrorCode;
import com.fernirx.sneakerapi.common.exception.BusinessException;
import com.fernirx.sneakerapi.notification.event.LowStockEvent;
import com.fernirx.sneakerapi.notification.event.OutOfStockEvent;
import com.fernirx.sneakerapi.product.assembler.ProductAssembler;
import com.fernirx.sneakerapi.product.dto.request.CreateVariantRequest;
import com.fernirx.sneakerapi.product.dto.request.UpdateVariantRequest;
import com.fernirx.sneakerapi.product.dto.response.ProductVariantGroupResponse;
import com.fernirx.sneakerapi.product.dto.response.StockChangeResult;
import com.fernirx.sneakerapi.product.dto.response.VariantSearchResponse;
import com.fernirx.sneakerapi.product.entity.Product;
import com.fernirx.sneakerapi.product.entity.ProductVariant;
import com.fernirx.sneakerapi.product.mapper.ProductVariantMapper;
import com.fernirx.sneakerapi.product.repository.ProductRepository;
import com.fernirx.sneakerapi.product.repository.ProductVariantRepository;
import com.fernirx.sneakerapi.product.repository.ProductVariantSpec;
import com.fernirx.sneakerapi.product.service.ProductDeletionPolicy;
import com.fernirx.sneakerapi.product.service.ProductVariantService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class ProductVariantServiceImpl implements ProductVariantService {

    private final ProductVariantRepository productVariantRepository;
    private final ProductRepository productRepository;
    private final ProductVariantMapper productVariantMapper;
    private final ProductAssembler productAssembler;
    private final ApplicationEventPublisher eventPublisher;
    private final ProductDeletionPolicy productDeletionPolicy;

    /**
     * Tìm một biến thể đang hoạt động (Active) theo ID.
     * Dành cho các tác vụ Storefront (khách hàng chỉ được xem biến thể đang active).
     */
    @Override
    @Transactional(readOnly = true)
    public ProductVariant findActiveById(Long id) {
        ProductVariant variant = productVariantRepository.findById(id)
                .orElseThrow(() -> BusinessException.notFound("label.product.variant"));
        if (!variant.getActive()) {
            throw BusinessException.notFound("label.product.variant");
        }
        return variant;
    }

    /**
     * Tìm danh sách các biến thể đang hoạt động theo danh sách ID.
     * Sử dụng để xác thực các mặt hàng trong giỏ hàng hoặc trước khi tạo đơn hàng.
     */
    @Override
    @Transactional(readOnly = true)
    public List<ProductVariant> findAllActiveByIds(List<Long> ids) {
        List<ProductVariant> variants = productVariantRepository.findByIdInAndActiveTrue(ids);
        if (variants.size() != ids.stream().distinct().count()) {
            throw BusinessException.notFound("label.product.variant");
        }
        return variants;
    }

    /**
     * Tìm biến thể theo ID bất kể trạng thái (Active/Inactive).
     * Dành cho các tác vụ CMS nội bộ (Admin/Staff).
     */
    @Override
    @Transactional(readOnly = true)
    public ProductVariant findById(Long id) {
        return productVariantRepository.findById(id)
                .orElseThrow(() -> BusinessException.notFound("label.product.variant"));
    }

    /**
     * Lấy danh sách các biến thể của một sản phẩm, nhóm theo Colorway (màu sắc).
     */
    @Override
    @Transactional(readOnly = true)
    public List<ProductVariantGroupResponse> getVariants(Long productId) {
        findProduct(productId);
        List<ProductVariant> variants = productVariantRepository
                .findByProductIdOrderByColorwayAscSizeAsc(productId);
        return productAssembler.toVariantGroups(variants);
    }

    /**
     * Lấy danh sách các biến thể của một sản phẩm dành cho Staff/Admin.
     * Dựa trên Role của người gọi, tự động che giấu CostPrice nếu không phải Admin.
     */
    @Override
    @Transactional(readOnly = true)
    public List<ProductVariantGroupResponse> getVariantsForStaff(Long productId, Collection<String> callerRoles) {
        List<ProductVariantGroupResponse> groups = getVariants(productId);
        return callerRoles.contains("ROLE_ADMIN") ? groups : maskCostPrice(groups);
    }

    /**
     * Helper lọc và che giấu giá nhập (CostPrice) khi trả về cho các Role không phải Admin (như Sale, Warehouse).
     */
    private List<ProductVariantGroupResponse> maskCostPrice(List<ProductVariantGroupResponse> groups) {
        return groups.stream()
                .map(g -> new ProductVariantGroupResponse(
                        g.colorway(), g.colorwayCode(), g.colorHex(),
                        g.variants().stream()
                                .map(v -> new ProductVariantGroupResponse.VariantResponse(
                                        v.id(), v.size(), v.shoeWidth(), v.sku(), v.price(), v.originalPrice(),
                                        null,
                                        v.stockQuantity(), v.minStockLevel(), v.weight(), v.length(), v.width(), v.height(),
                                        v.displayOrder(), v.active()))
                                .toList()))
                .toList();
    }

    /**
     * Tìm kiếm nhanh các biến thể theo từ khóa (SKU, tên sản phẩm).
     * Phục vụ cho tính năng tìm kiếm của nhân viên tại quầy (POS) hoặc CMS.
     */
    @Override
    @Transactional(readOnly = true)
    public Page<VariantSearchResponse> searchVariants(String keyword, Pageable pageable) {
        return productVariantRepository.findAll(ProductVariantSpec.build(keyword), pageable)
                .map(productVariantMapper::toSearchResponse);
    }

    /**
     * Thêm mới một biến thể (Variant) cho sản phẩm.
     * Quy tắc bảo vệ: 
     * 1. SKU phải là duy nhất trên toàn hệ thống.
     * 2. Tổ hợp (ProductId, Size, Colorway, ShoeWidth) phải là duy nhất để tránh tạo biến thể trùng lặp.
     * 3. Giá bán phải hợp lệ (lớn hơn 0), giá gốc (nếu có) phải lớn hơn hoặc bằng giá bán. Nếu vi phạm, ép hạ cờ active = false.
     * 4. Gọi syncProductPrices để cập nhật lại khoảng giá cho Product mẹ.
     */
    @Override
    public ProductVariantGroupResponse.VariantResponse addVariant(Long productId, CreateVariantRequest request) {
        Product product = findProduct(productId);

        if (productVariantRepository.existsBySku(request.sku())) {
            throw BusinessException.alreadyExists("label.product.variant");
        }
        if (productVariantRepository.existsByProductIdAndSizeAndColorwayAndShoeWidth(
                productId, request.size(), request.colorway(), request.shoeWidth())) {
            throw BusinessException.alreadyExists("label.product.variant");
        }

        ProductVariant variant = new ProductVariant();
        variant.setProduct(product);
        variant.setSize(request.size());
        variant.setShoeWidth(request.shoeWidth());
        variant.setColorway(request.colorway());
        variant.setColorwayCode(request.colorwayCode());
        variant.setColorHex(request.colorHex());
        variant.setPrice(request.price());
        variant.setSku(request.sku());
        variant.setStockQuantity(request.stockQuantity());
        variant.setMinStockLevel(request.minStockLevel() != null ? request.minStockLevel() : 5);
        variant.setWeight(request.weight() != null ? request.weight() : 800);
        variant.setLength(request.length() != null ? request.length() : 33);
        variant.setWidth(request.width() != null ? request.width() : 22);
        variant.setHeight(request.height() != null ? request.height() : 12);
        variant.setDisplayOrder(request.displayOrder() != null ? request.displayOrder() : 0);
        variant.setOriginalPrice(request.originalPrice());
        variant.setCostPrice(request.costPrice());
        
        boolean isValidPrice = variant.getPrice() != null && variant.getPrice().compareTo(java.math.BigDecimal.ZERO) > 0;
        boolean isValidOriginal = variant.getOriginalPrice() == null || (variant.getPrice() != null && variant.getOriginalPrice().compareTo(variant.getPrice()) >= 0);
        boolean requestedActive = request.active() != null ? request.active() : true;
        variant.setActive(requestedActive && isValidPrice && isValidOriginal);

        ProductVariant saved = productVariantRepository.save(variant);
        syncProductPrices(product);
        return productVariantMapper.toVariantResponse(saved);
    }

    /**
     * Cập nhật thông tin của một biến thể.
     * Quy tắc bảo vệ:
     * 1. Tương tự addVariant, nếu SKU bị đổi thì SKU mới không được trùng với Variant khác.
     * 2. Validate lại Giá bán & Giá gốc. Nếu không hợp lệ mà Variant đang active -> ép hạ active = false.
     * 3. Sync lại giá min/max của Product mẹ.
     */
    @Override
    public ProductVariantGroupResponse.VariantResponse updateVariant(Long productId, Long variantId, UpdateVariantRequest request) {
        findProduct(productId);
        ProductVariant variant = findVariant(productId, variantId);

        if (request.sku() != null && !request.sku().equals(variant.getSku())) {
            if (productVariantRepository.existsBySkuAndIdNot(request.sku(), variantId)) {
                throw BusinessException.alreadyExists("label.product.variant");
            }
        }

        productVariantMapper.updateVariant(request, variant);

        boolean isValidPrice = variant.getPrice() != null && variant.getPrice().compareTo(java.math.BigDecimal.ZERO) > 0;
        boolean isValidOriginal = variant.getOriginalPrice() == null || (variant.getPrice() != null && variant.getOriginalPrice().compareTo(variant.getPrice()) >= 0);
        if (Boolean.TRUE.equals(variant.getActive()) && (!isValidPrice || !isValidOriginal)) {
            variant.setActive(false);
        }

        ProductVariant saved = productVariantRepository.save(variant);
        syncProductPrices(variant.getProduct());
        return productVariantMapper.toVariantResponse(saved);
    }

    /**
     * Xóa cứng một biến thể (Variant) của sản phẩm.
     * Luồng xử lý:
     * 1. Tìm biến thể theo ID và xác nhận nó thuộc về Product tương ứng.
     * 2. Gọi ProductDeletionPolicy để kiểm tra xem biến thể này có đang nằm trong đơn hàng, phiếu nhập, 
     *    hay lịch sử kho nào không. Nếu có, từ chối xóa để bảo toàn dữ liệu ngoại lai.
     * 3. Xóa biến thể và ép flush xuống DB ngay lập tức.
     * 4. Gọi hàm syncProductPrices để cập nhật lại khoảng giá (min/max price) của Product mẹ.
     */
    @Override
    public void deleteVariant(Long productId, Long variantId) {
        findProduct(productId);
        ProductVariant variant = findVariant(productId, variantId);
        
        productDeletionPolicy.validateVariantDeletion(variantId);
        
        productVariantRepository.delete(variant);
        productVariantRepository.flush();
        syncProductPrices(variant.getProduct());
    }

    /**
     * Trừ tồn kho một cách an toàn (Atomic Decrease).
     * Luồng xử lý:
     * 1. Dùng truy vấn Update với điều kiện stock_quantity >= quantity để khóa row và cập nhật, tránh Race Condition 100%.
     * 2. Nếu không cập nhật được (updatedRows = 0):
     *    - Nếu Variant không tồn tại -> ném lỗi Not Found.
     *    - Nếu Variant tồn tại nhưng không đủ số lượng -> ném lỗi hết hàng.
     * 3. Bắn sự kiện (LowStockEvent hoặc OutOfStockEvent) nếu tồn kho chạm ngưỡng cảnh báo hoặc về 0.
     */
    @Override
    public StockChangeResult decreaseStock(Long variantId, int quantity) {
        int updatedRows = productVariantRepository.decreaseStockAtomic(variantId, quantity);
        if (updatedRows == 0) {
            if (!productVariantRepository.existsById(variantId)) {
                throw BusinessException.notFound("label.product.variant");
            }
            throw BusinessException.bad("label.product.stock");
        }
        int newStock = productVariantRepository.findStockQuantityById(variantId)
                .orElseThrow(() -> BusinessException.notFound("label.product.variant"));

        publishStockThresholdEvent(variantId, newStock);

        return new StockChangeResult(variantId, newStock + quantity, newStock);
    }

    /**
     * Helper kiểm tra tồn kho và phát sự kiện cảnh báo sắp hết hoặc đã hết hàng.
     */
    private void publishStockThresholdEvent(Long variantId, int newStock) {
        if (newStock > 0) {
            ProductVariant variant = productVariantRepository.findById(variantId).orElse(null);
            if (variant != null && newStock <= variant.getMinStockLevel()) {
                eventPublisher.publishEvent(new LowStockEvent(
                        variantId, variant.getProduct().getName(), variant.getSku(), newStock, variant.getMinStockLevel()));
            }
        } else if (newStock == 0) {
            ProductVariant variant = productVariantRepository.findById(variantId).orElse(null);
            if (variant != null) {
                eventPublisher.publishEvent(new OutOfStockEvent(variantId, variant.getProduct().getName(), variant.getSku()));
            }
        }
    }

    /**
     * Cộng tồn kho an toàn (Atomic Increase).
     * Dùng truy vấn Update trực tiếp vào DB để cộng dồn, tránh Race Condition.
     */
    @Override
    public StockChangeResult increaseStock(Long variantId, int quantity) {
        int updatedRows = productVariantRepository.increaseStockAtomic(variantId, quantity);
        if (updatedRows == 0) {
            throw BusinessException.notFound("label.product.variant");
        }
        int newStock = productVariantRepository.findStockQuantityById(variantId)
                .orElseThrow(() -> BusinessException.notFound("label.product.variant"));
        return new StockChangeResult(variantId, newStock - quantity, newStock);
    }

    /**
     * Helper tìm sản phẩm theo ID, ném ngoại lệ chung nếu không thấy.
     */
    private Product findProduct(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> BusinessException.notFound("label.product"));
    }

    /**
     * Helper tìm biến thể theo Variant ID và Product ID tương ứng.
     */
    private ProductVariant findVariant(Long productId, Long variantId) {
        return productVariantRepository.findByIdAndProductId(variantId, productId)
                .orElseThrow(() -> BusinessException.notFound("label.product.variant"));
    }

    /**
     * Helper tự động đồng bộ lại khoảng giá (minPrice, maxPrice) và trạng thái active của Product mẹ
     * mỗi khi có một biến thể được thêm/sửa/xóa.
     * Nếu không còn biến thể active nào, ép hạ active của Product = false.
     */
    private void syncProductPrices(Product product) {
        List<ProductVariant> activeVariants = productVariantRepository.findByProductIdAndActiveTrueOrderByDisplayOrderAsc(product.getId());
        if (activeVariants.isEmpty()) {
            product.setMinPrice(null);
            product.setMaxPrice(null);
            product.setActive(false);
        } else {
            java.math.BigDecimal min = activeVariants.stream().map(ProductVariant::getPrice).min(java.math.BigDecimal::compareTo).orElse(null);
            java.math.BigDecimal max = activeVariants.stream().map(ProductVariant::getPrice).max(java.math.BigDecimal::compareTo).orElse(null);
            product.setMinPrice(min);
            product.setMaxPrice(max);
        }
        productRepository.save(product);
    }
}
