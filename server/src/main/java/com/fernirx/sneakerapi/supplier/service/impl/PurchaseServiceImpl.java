package com.fernirx.sneakerapi.supplier.service.impl;

import com.fernirx.sneakerapi.common.exception.BusinessException;
import com.fernirx.sneakerapi.inventory.enums.InventoryReferenceType;
import com.fernirx.sneakerapi.inventory.enums.InventoryTransactionType;
import com.fernirx.sneakerapi.inventory.service.InventoryTransactionService;
import com.fernirx.sneakerapi.product.dto.response.StockChangeResult;
import com.fernirx.sneakerapi.product.entity.ProductVariant;
import com.fernirx.sneakerapi.product.service.ProductVariantService;
import com.fernirx.sneakerapi.supplier.dto.request.CancelPurchaseRequest;
import com.fernirx.sneakerapi.supplier.dto.request.CreatePurchaseRequest;
import com.fernirx.sneakerapi.supplier.dto.request.PurchaseFilterRequest;
import com.fernirx.sneakerapi.supplier.dto.request.PurchaseItemRequest;
import com.fernirx.sneakerapi.supplier.dto.request.ReceivePurchaseItemRequest;
import com.fernirx.sneakerapi.supplier.dto.request.ReceivePurchaseRequest;
import com.fernirx.sneakerapi.supplier.dto.request.UpdatePurchaseRequest;
import com.fernirx.sneakerapi.supplier.dto.response.PurchaseItemResponse;
import com.fernirx.sneakerapi.supplier.dto.response.PurchaseResponse;
import com.fernirx.sneakerapi.supplier.entity.Purchase;
import com.fernirx.sneakerapi.supplier.entity.PurchaseItem;
import com.fernirx.sneakerapi.supplier.entity.Supplier;
import com.fernirx.sneakerapi.supplier.enums.PurchasePaymentStatus;
import com.fernirx.sneakerapi.supplier.enums.PurchaseStatus;
import com.fernirx.sneakerapi.supplier.mapper.PurchaseMapper;
import com.fernirx.sneakerapi.supplier.repository.PurchaseItemRepository;
import com.fernirx.sneakerapi.supplier.repository.PurchaseRepository;
import com.fernirx.sneakerapi.supplier.repository.PurchaseSpec;
import com.fernirx.sneakerapi.supplier.repository.SupplierRepository;
import com.fernirx.sneakerapi.supplier.service.PurchaseService;
import com.fernirx.sneakerapi.user.entity.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class PurchaseServiceImpl implements PurchaseService {

    private final PurchaseRepository purchaseRepository;
    private final PurchaseItemRepository purchaseItemRepository;
    private final SupplierRepository supplierRepository;
    private final PurchaseMapper purchaseMapper;
    private final ProductVariantService productVariantService;
    private final InventoryTransactionService inventoryTransactionService;

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @Transactional(readOnly = true)
    public Page<PurchaseResponse> getAll(PurchaseFilterRequest filter, Pageable pageable) {
        return purchaseRepository.findAll(PurchaseSpec.build(filter), pageable)
                .map(purchase -> purchaseMapper.toResponse(purchase, mapItems(purchase)));
    }

    @Override
    @Transactional(readOnly = true)
    public PurchaseResponse getById(Long id) {
        Purchase purchase = findById(id);
        return purchaseMapper.toResponse(purchase, mapItems(purchase));
    }

    @Override
    public PurchaseResponse create(CreatePurchaseRequest request, Long createdByUserId) {
        Supplier supplier = findSupplier(request.supplierId());

        Purchase purchase = new Purchase();
        purchase.setSupplier(supplier);
        purchase.setCreatedBy(createdByUserId != null ? entityManager.getReference(User.class, createdByUserId) : null);
        purchase.setPurchaseCode(generatePurchaseCode());
        purchase.setSupplierInvoiceNo(request.supplierInvoiceNo());
        purchase.setDiscountAmount(zeroIfNull(request.discountAmount()));
        purchase.setTaxAmount(zeroIfNull(request.taxAmount()));
        purchase.setShippingCost(zeroIfNull(request.shippingCost()));
        purchase.setSubtotal(BigDecimal.ZERO);
        purchase.setTotalCost(BigDecimal.ZERO);
        purchase.setPaymentStatus(PurchasePaymentStatus.UNPAID);
        purchase.setStatus(PurchaseStatus.DRAFT);
        purchase.setNotes(request.notes());
        purchase = purchaseRepository.save(purchase);

        applyItems(purchase, request.items());
        purchase = purchaseRepository.save(purchase);

        return purchaseMapper.toResponse(purchase, mapItems(purchase));
    }

    @Override
    public PurchaseResponse update(Long id, UpdatePurchaseRequest request) {
        Purchase purchase = findById(id);
        requireStatus(purchase, PurchaseStatus.DRAFT);

        if (request.supplierId() != null) {
            purchase.setSupplier(findSupplier(request.supplierId()));
        }
        if (request.supplierInvoiceNo() != null) {
            purchase.setSupplierInvoiceNo(request.supplierInvoiceNo());
        }
        if (request.discountAmount() != null) {
            purchase.setDiscountAmount(request.discountAmount());
        }
        if (request.taxAmount() != null) {
            purchase.setTaxAmount(request.taxAmount());
        }
        if (request.shippingCost() != null) {
            purchase.setShippingCost(request.shippingCost());
        }
        if (request.notes() != null) {
            purchase.setNotes(request.notes());
        }

        if (request.items() != null) {
            if (request.items().isEmpty()) {
                throw BusinessException.bad("label.purchase.item");
            }
            applyItems(purchase, request.items());
        } else {
            recalcTotalCost(purchase);
        }

        purchase = purchaseRepository.save(purchase);
        return purchaseMapper.toResponse(purchase, mapItems(purchase));
    }

    @Override
    public PurchaseResponse confirm(Long id) {
        Purchase purchase = findById(id);
        requireStatus(purchase, PurchaseStatus.DRAFT);

        purchase.setStatus(PurchaseStatus.CONFIRMED);
        purchase.setConfirmedAt(LocalDateTime.now());
        purchase = purchaseRepository.save(purchase);
        return purchaseMapper.toResponse(purchase, mapItems(purchase));
    }

    @Override
    public PurchaseResponse receive(Long id, ReceivePurchaseRequest request, Long receivedByUserId) {
        Purchase purchase = findById(id);
        requireStatus(purchase, PurchaseStatus.CONFIRMED);

        List<PurchaseItem> items = purchaseItemRepository.findAllByPurchase(purchase);
        Map<Long, PurchaseItem> itemById = items.stream()
                .collect(Collectors.toMap(PurchaseItem::getId, Function.identity()));

        boolean sameSize = request.items().size() == items.size();
        boolean allKnown = request.items().stream()
                .map(ReceivePurchaseItemRequest::purchaseItemId)
                .allMatch(itemById::containsKey);
        if (!sameSize || !allKnown) {
            throw BusinessException.bad("label.purchase.item");
        }

        for (ReceivePurchaseItemRequest receiveItem : request.items()) {
            if (receiveItem.defectiveQty() > receiveItem.quantityReceived()) {
                throw BusinessException.bad("label.purchase.item");
            }

            PurchaseItem item = itemById.get(receiveItem.purchaseItemId());
            item.setQuantityReceived(receiveItem.quantityReceived());
            item.setDefectiveQty(receiveItem.defectiveQty());
            if (receiveItem.note() != null) {
                item.setNotes(receiveItem.note());
            }
            purchaseItemRepository.save(item);

            int sellableQty = receiveItem.quantityReceived() - receiveItem.defectiveQty();
            if (sellableQty > 0) {
                Long variantId = item.getVariant().getId();
                StockChangeResult stockChange = productVariantService.increaseStock(variantId, sellableQty);
                inventoryTransactionService.record(variantId, receivedByUserId, InventoryTransactionType.IN, sellableQty,
                        stockChange.oldStock(), stockChange.newStock(), InventoryReferenceType.PURCHASE_ITEM, item.getId(),
                        "Nhập kho từ phiếu nhập #" + purchase.getPurchaseCode());
            }
        }

        purchase.setStatus(PurchaseStatus.RECEIVED);
        purchase.setReceivedAt(LocalDateTime.now());
        purchase.setReceivedBy(receivedByUserId != null ? entityManager.getReference(User.class, receivedByUserId) : null);
        purchase = purchaseRepository.save(purchase);

        return purchaseMapper.toResponse(purchase, mapItems(purchase));
    }

    @Override
    public PurchaseResponse cancel(Long id, CancelPurchaseRequest request) {
        Purchase purchase = findById(id);
        if (purchase.getStatus() != PurchaseStatus.DRAFT && purchase.getStatus() != PurchaseStatus.CONFIRMED) {
            throw BusinessException.bad("label.purchase");
        }

        purchase.setStatus(PurchaseStatus.CANCELLED);
        String reason = request != null ? request.reason() : null;
        if (StringUtils.hasText(reason)) {
            purchase.setNotes(StringUtils.hasText(purchase.getNotes())
                    ? purchase.getNotes() + " | Hủy: " + reason
                    : "Hủy: " + reason);
        }
        purchase = purchaseRepository.save(purchase);
        return purchaseMapper.toResponse(purchase, mapItems(purchase));
    }

    @Override
    public PurchaseResponse markAsPaid(Long id) {
        Purchase purchase = findById(id);
        purchase.setPaymentStatus(PurchasePaymentStatus.PAID);
        purchase = purchaseRepository.save(purchase);
        return purchaseMapper.toResponse(purchase, mapItems(purchase));
    }

    // ---- Private helpers ----

    private void applyItems(Purchase purchase, List<PurchaseItemRequest> itemRequests) {
        List<PurchaseItem> existing = purchaseItemRepository.findAllByPurchase(purchase);
        if (!existing.isEmpty()) {
            purchaseItemRepository.deleteAll(existing);
        }

        BigDecimal subtotal = BigDecimal.ZERO;
        for (PurchaseItemRequest itemRequest : itemRequests) {
            ProductVariant variant = productVariantService.findById(itemRequest.variantId());
            BigDecimal lineTotal = itemRequest.unitCost().multiply(BigDecimal.valueOf(itemRequest.quantityOrdered()));

            PurchaseItem item = new PurchaseItem();
            item.setPurchase(purchase);
            item.setVariant(variant);
            item.setQuantityOrdered(itemRequest.quantityOrdered());
            item.setQuantityReceived(0);
            item.setDefectiveQty(0);
            item.setUnitCost(itemRequest.unitCost());
            item.setLineTotal(lineTotal);
            purchaseItemRepository.save(item);

            subtotal = subtotal.add(lineTotal);
        }

        purchase.setSubtotal(subtotal);
        recalcTotalCost(purchase);
    }

    private void recalcTotalCost(Purchase purchase) {
        BigDecimal total = purchase.getSubtotal()
                .subtract(purchase.getDiscountAmount())
                .add(purchase.getTaxAmount())
                .add(purchase.getShippingCost());
        purchase.setTotalCost(total);
    }

    private void requireStatus(Purchase purchase, PurchaseStatus expected) {
        if (purchase.getStatus() != expected) {
            throw BusinessException.bad("label.purchase");
        }
    }

    private BigDecimal zeroIfNull(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }

    private String generatePurchaseCode() {
        return "PO" + System.currentTimeMillis() + ThreadLocalRandom.current().nextInt(100, 999);
    }

    private List<PurchaseItemResponse> mapItems(Purchase purchase) {
        return purchaseItemRepository.findAllByPurchase(purchase).stream()
                .map(purchaseMapper::toItemResponse).toList();
    }

    private Supplier findSupplier(Long id) {
        return supplierRepository.findById(id)
                .orElseThrow(() -> BusinessException.notFound("label.supplier"));
    }

    private Purchase findById(Long id) {
        return purchaseRepository.findById(id)
                .orElseThrow(() -> BusinessException.notFound("label.purchase"));
    }
}
