package com.fernirx.sneakerapi.inventory.service.impl;

import com.fernirx.sneakerapi.common.exception.BusinessException;
import com.fernirx.sneakerapi.inventory.dto.request.CancelStockAdjustmentRequest;
import com.fernirx.sneakerapi.inventory.dto.request.CreateStockAdjustmentRequest;
import com.fernirx.sneakerapi.inventory.dto.request.StockAdjustmentFilterRequest;
import com.fernirx.sneakerapi.inventory.dto.request.StockAdjustmentItemRequest;
import com.fernirx.sneakerapi.inventory.dto.request.UpdateStockAdjustmentRequest;
import com.fernirx.sneakerapi.inventory.dto.response.StockAdjustmentItemResponse;
import com.fernirx.sneakerapi.inventory.dto.response.StockAdjustmentResponse;
import com.fernirx.sneakerapi.inventory.entity.StockAdjustment;
import com.fernirx.sneakerapi.inventory.entity.StockAdjustmentItem;
import com.fernirx.sneakerapi.inventory.enums.InventoryReferenceType;
import com.fernirx.sneakerapi.inventory.enums.InventoryTransactionType;
import com.fernirx.sneakerapi.inventory.enums.StockAdjustmentStatus;
import com.fernirx.sneakerapi.inventory.mapper.StockAdjustmentMapper;
import com.fernirx.sneakerapi.inventory.repository.StockAdjustmentItemRepository;
import com.fernirx.sneakerapi.inventory.repository.StockAdjustmentRepository;
import com.fernirx.sneakerapi.inventory.repository.StockAdjustmentSpec;
import com.fernirx.sneakerapi.inventory.service.InventoryTransactionService;
import com.fernirx.sneakerapi.inventory.service.StockAdjustmentService;
import com.fernirx.sneakerapi.product.dto.response.StockChangeResult;
import com.fernirx.sneakerapi.product.entity.ProductVariant;
import com.fernirx.sneakerapi.product.service.ProductVariantService;
import com.fernirx.sneakerapi.user.entity.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Service
@Transactional
@RequiredArgsConstructor
public class StockAdjustmentServiceImpl implements StockAdjustmentService {

    private final StockAdjustmentRepository stockAdjustmentRepository;
    private final StockAdjustmentItemRepository stockAdjustmentItemRepository;
    private final StockAdjustmentMapper stockAdjustmentMapper;
    private final ProductVariantService productVariantService;
    private final InventoryTransactionService inventoryTransactionService;

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * Lấy danh sách phiếu Điều chỉnh kho (dùng cho CMS).
     * Hỗ trợ tìm kiếm, phân trang và tự động map danh sách item đi kèm.
     */
    @Override
    @Transactional(readOnly = true)
    public Page<StockAdjustmentResponse> getAll(StockAdjustmentFilterRequest filter, Pageable pageable) {
        return stockAdjustmentRepository.findAll(StockAdjustmentSpec.build(filter), pageable)
                .map(adjustment -> stockAdjustmentMapper.toResponse(adjustment, mapItems(adjustment)));
    }

    /**
     * Lấy chi tiết một phiếu Điều chỉnh kho dựa trên ID.
     */
    @Override
    @Transactional(readOnly = true)
    public StockAdjustmentResponse getById(Long id) {
        StockAdjustment adjustment = findById(id);
        return stockAdjustmentMapper.toResponse(adjustment, mapItems(adjustment));
    }

    /**
     * Tạo mới một phiếu Điều chỉnh kho.
     * Trạng thái mặc định ban đầu là DRAFT (Nháp).
     * Số lượng trước/sau tại bước này chỉ mang tính ước lượng dự kiến, 
     * số liệu thực tế sẽ được chốt tại thời điểm Xác nhận (CONFIRMED).
     */
    @Override
    public StockAdjustmentResponse create(CreateStockAdjustmentRequest request, Long createdByUserId) {
        StockAdjustment adjustment = new StockAdjustment();
        adjustment.setCreatedBy(createdByUserId != null ? entityManager.getReference(User.class, createdByUserId) : null);
        adjustment.setCode(generateCode());
        adjustment.setType(request.type());
        adjustment.setStatus(StockAdjustmentStatus.DRAFT);
        adjustment.setReason(request.reason());
        adjustment = stockAdjustmentRepository.save(adjustment);

        applyItems(adjustment, request.items());

        return stockAdjustmentMapper.toResponse(adjustment, mapItems(adjustment));
    }

    /**
     * Cập nhật thông tin phiếu Điều chỉnh kho.
     * Chỉ cho phép cập nhật khi phiếu đang ở trạng thái DRAFT.
     */
    @Override
    public StockAdjustmentResponse update(Long id, UpdateStockAdjustmentRequest request) {
        StockAdjustment adjustment = findById(id);
        requireStatus(adjustment, StockAdjustmentStatus.DRAFT);

        if (request.type() != null) {
            adjustment.setType(request.type());
        }
        if (request.reason() != null) {
            adjustment.setReason(request.reason());
        }
        adjustment = stockAdjustmentRepository.save(adjustment);

        if (request.items() != null) {
            if (request.items().isEmpty()) {
                throw BusinessException.bad("label.stock_adjustment.item");
            }
            applyItems(adjustment, request.items());
        }

        return stockAdjustmentMapper.toResponse(adjustment, mapItems(adjustment));
    }

    /**
     * Phê duyệt (Approve) phiếu Điều chỉnh kho.
     * Phiếu chuyển từ DRAFT -> APPROVED. Chưa làm thay đổi tồn kho thực tế.
     */
    @Override
    public StockAdjustmentResponse approve(Long id, Long approvedByUserId) {
        StockAdjustment adjustment = findById(id);
        requireStatus(adjustment, StockAdjustmentStatus.DRAFT);

        adjustment.setStatus(StockAdjustmentStatus.APPROVED);
        adjustment.setApprovedAt(LocalDateTime.now());
        adjustment.setApprovedBy(approvedByUserId != null ? entityManager.getReference(User.class, approvedByUserId) : null);
        adjustment = stockAdjustmentRepository.save(adjustment);

        return stockAdjustmentMapper.toResponse(adjustment, mapItems(adjustment));
    }

    /**
     * Xác nhận (Confirm) và thi hành phiếu Điều chỉnh kho.
     * Luồng xử lý cực kỳ quan trọng:
     * 1. Chỉ được xác nhận khi phiếu đang ở trạng thái APPROVED.
     * 2. Quét qua từng Item, gọi ProductVariantService để cộng/trừ tồn kho thực tế.
     *    (ProductVariantService sẽ khóa row và đảm bảo tính nguyên vẹn dữ liệu).
     * 3. Lấy số tồn kho cũ/mới THỰC TẾ từ ProductVariantService để ghi đè lại vào Item (vì tồn kho có thể đã đổi từ lúc tạo DRAFT).
     * 4. Ghi log lịch sử biến động kho (Inventory Transaction) cho từng Item.
     * 5. Chuyển trạng thái phiếu sang CONFIRMED.
     */
    @Override
    public StockAdjustmentResponse confirm(Long id, Long confirmedByUserId) {
        StockAdjustment adjustment = findById(id);
        requireStatus(adjustment, StockAdjustmentStatus.APPROVED);

        for (StockAdjustmentItem item : stockAdjustmentItemRepository.findAllByAdjustment(adjustment)) {
            int change = item.getQuantityChange();
            Long variantId = item.getVariant().getId();
            StockChangeResult stockChange = change >= 0
                    ? productVariantService.increaseStock(variantId, change)
                    : productVariantService.decreaseStock(variantId, -change);

            item.setQuantityBefore(stockChange.oldStock());
            item.setQuantityAfter(stockChange.newStock());
            stockAdjustmentItemRepository.save(item);

            inventoryTransactionService.record(variantId, confirmedByUserId, InventoryTransactionType.ADJUST, change,
                    stockChange.oldStock(), stockChange.newStock(), InventoryReferenceType.STOCK_ADJUSTMENT, item.getId(),
                    "Điều chỉnh kho từ phiếu #" + adjustment.getCode());
        }

        adjustment.setStatus(StockAdjustmentStatus.CONFIRMED);
        adjustment.setConfirmedAt(LocalDateTime.now());
        adjustment = stockAdjustmentRepository.save(adjustment);

        return stockAdjustmentMapper.toResponse(adjustment, mapItems(adjustment));
    }

    /**
     * Hủy (Cancel) phiếu Điều chỉnh kho.
     * Chỉ được hủy khi phiếu đang ở DRAFT hoặc APPROVED (chưa confirm).
     */
    @Override
    public StockAdjustmentResponse cancel(Long id, CancelStockAdjustmentRequest request) {
        StockAdjustment adjustment = findById(id);
        if (adjustment.getStatus() != StockAdjustmentStatus.DRAFT && adjustment.getStatus() != StockAdjustmentStatus.APPROVED) {
            throw BusinessException.bad("label.stock_adjustment");
        }

        adjustment.setStatus(StockAdjustmentStatus.CANCELLED);
        String reason = request != null ? request.reason() : null;
        if (reason != null) {
            adjustment.setReason(adjustment.getReason() + " | Hủy: " + reason);
        }
        adjustment = stockAdjustmentRepository.save(adjustment);

        return stockAdjustmentMapper.toResponse(adjustment, mapItems(adjustment));
    }

    // ---- Private helpers ----

    /**
     * Xóa list item cũ (nếu có) và apply list item mới vào phiếu.
     * Tính toán số lượng Trước/Sau mang tính chất ước lượng tại thời điểm gọi hàm.
     */
    private void applyItems(StockAdjustment adjustment, List<StockAdjustmentItemRequest> itemRequests) {
        List<StockAdjustmentItem> existing = stockAdjustmentItemRepository.findAllByAdjustment(adjustment);
        if (!existing.isEmpty()) {
            stockAdjustmentItemRepository.deleteAll(existing);
        }

        for (StockAdjustmentItemRequest itemRequest : itemRequests) {
            if (itemRequest.quantityChange() == 0) {
                throw BusinessException.bad("label.stock_adjustment.item");
            }
            ProductVariant variant = productVariantService.findById(itemRequest.variantId());
            int currentStock = variant.getStockQuantity();

            StockAdjustmentItem item = new StockAdjustmentItem();
            item.setAdjustment(adjustment);
            item.setVariant(variant);
            item.setQuantityChange(itemRequest.quantityChange());
            item.setQuantityBefore(currentStock);
            item.setQuantityAfter(currentStock + itemRequest.quantityChange());
            item.setNote(itemRequest.note());
            stockAdjustmentItemRepository.save(item);
        }
    }

    /**
     * Helper kiểm tra trạng thái của phiếu Điều chỉnh kho.
     * Văng lỗi BusinessException nếu không khớp trạng thái kỳ vọng.
     */
    private void requireStatus(StockAdjustment adjustment, StockAdjustmentStatus expected) {
        if (adjustment.getStatus() != expected) {
            throw BusinessException.bad("label.stock_adjustment");
        }
    }

    /**
     * Helper sinh mã phiếu Điều chỉnh ngẫu nhiên.
     * Định dạng: ADJ + timestamp + 3 số ngẫu nhiên.
     */
    private String generateCode() {
        return "ADJ" + System.currentTimeMillis() + ThreadLocalRandom.current().nextInt(100, 999);
    }

    /**
     * Helper map danh sách Item Entity sang DTO Response để trả về cho Frontend.
     */
    private List<StockAdjustmentItemResponse> mapItems(StockAdjustment adjustment) {
        return stockAdjustmentItemRepository.findAllByAdjustment(adjustment).stream()
                .map(stockAdjustmentMapper::toItemResponse).toList();
    }

    /**
     * Helper tìm phiếu Điều chỉnh kho theo ID, ném lỗi 404 nếu không thấy.
     */
    private StockAdjustment findById(Long id) {
        return stockAdjustmentRepository.findById(id)
                .orElseThrow(() -> BusinessException.notFound("label.stock_adjustment"));
    }
}
