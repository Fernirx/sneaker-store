package com.fernirx.sneakerapi.product.repository;

import com.fernirx.sneakerapi.product.entity.ProductVariant;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductVariantRepository extends JpaRepository<ProductVariant, Long>, JpaSpecificationExecutor<ProductVariant> {

    List<ProductVariant> findByProductIdAndActiveTrueOrderByDisplayOrderAsc(Long productId);

    List<ProductVariant> findByProductIdInAndActiveTrueOrderByDisplayOrderAsc(List<Long> productIds);

    List<ProductVariant> findByIdInAndActiveTrue(List<Long> ids);

    List<ProductVariant> findByProductIdOrderByColorwayAscSizeAsc(Long productId);

    Optional<ProductVariant> findByIdAndProductId(Long id, Long productId);

    boolean existsBySku(String sku);

    boolean existsBySkuAndIdNot(String sku, Long id);

    boolean existsByProductIdAndActiveTrue(Long productId);

    boolean existsByProductIdAndSizeAndColorwayAndShoeWidth(Long productId, Short size, String colorway, Object shoeWidth);

    @Query("SELECT v.stockQuantity FROM ProductVariant v WHERE v.id = :id")
    Optional<Integer> findStockQuantityById(@Param("id") Long id);

    @Modifying
    @Query("UPDATE ProductVariant v SET v.stockQuantity = v.stockQuantity - :quantity " +
            "WHERE v.id = :id AND v.stockQuantity >= :quantity")
    int decreaseStockAtomic(@Param("id") Long id, @Param("quantity") int quantity);

    @Modifying
    @Query("UPDATE ProductVariant v SET v.stockQuantity = v.stockQuantity + :quantity WHERE v.id = :id")
    int increaseStockAtomic(@Param("id") Long id, @Param("quantity") int quantity);

    @Query("SELECT COUNT(v) FROM ProductVariant v WHERE v.active = true AND v.stockQuantity > 0 AND v.stockQuantity <= v.minStockLevel")
    long countLowStock();

    @Query("SELECT COUNT(v) FROM ProductVariant v WHERE v.active = true AND v.stockQuantity = 0")
    long countOutOfStock();

    @Query("SELECT v FROM ProductVariant v JOIN FETCH v.product WHERE v.active = true AND v.stockQuantity <= v.minStockLevel ORDER BY v.stockQuantity ASC")
    List<ProductVariant> findLowStockVariants(Pageable pageable);

    // Pre-check trước khi xóa cứng 1 variant - chặn nếu đã từng phát sinh dữ liệu ở module khác (Order/
    // Inventory/Supplier), tránh để lỗi khóa ngoại thô từ DB (các FK này không có @OnDelete, mặc định RESTRICT).
    @Query("SELECT COUNT(oi) > 0 FROM OrderItem oi WHERE oi.variant.id = :variantId")
    boolean existsOrderItemByVariantId(@Param("variantId") Long variantId);

    @Query("SELECT COUNT(t) > 0 FROM InventoryTransaction t WHERE t.variant.id = :variantId")
    boolean existsInventoryTransactionByVariantId(@Param("variantId") Long variantId);

    @Query("SELECT COUNT(pi) > 0 FROM PurchaseItem pi WHERE pi.variant.id = :variantId")
    boolean existsPurchaseItemByVariantId(@Param("variantId") Long variantId);

    @Query("SELECT COUNT(sai) > 0 FROM StockAdjustmentItem sai WHERE sai.variant.id = :variantId")
    boolean existsStockAdjustmentItemByVariantId(@Param("variantId") Long variantId);

    // Tương tự nhưng gộp theo product - dùng khi xóa cả Product (kiểm tra qua TẤT CẢ variant của nó).
    @Query("SELECT COUNT(oi) > 0 FROM OrderItem oi WHERE oi.variant.product.id = :productId")
    boolean existsOrderItemByProductId(@Param("productId") Long productId);

    @Query("SELECT COUNT(t) > 0 FROM InventoryTransaction t WHERE t.variant.product.id = :productId")
    boolean existsInventoryTransactionByProductId(@Param("productId") Long productId);

    @Query("SELECT COUNT(pi) > 0 FROM PurchaseItem pi WHERE pi.variant.product.id = :productId")
    boolean existsPurchaseItemByProductId(@Param("productId") Long productId);

    @Query("SELECT COUNT(sai) > 0 FROM StockAdjustmentItem sai WHERE sai.variant.product.id = :productId")
    boolean existsStockAdjustmentItemByProductId(@Param("productId") Long productId);
}
