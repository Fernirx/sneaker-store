package com.fernirx.sneakerapi.returns.service.impl;

import com.fernirx.sneakerapi.common.enums.ErrorCode;
import com.fernirx.sneakerapi.common.exception.BusinessException;
import com.fernirx.sneakerapi.customer.service.CustomerService;
import com.fernirx.sneakerapi.inventory.enums.InventoryReferenceType;
import com.fernirx.sneakerapi.inventory.enums.InventoryTransactionType;
import com.fernirx.sneakerapi.inventory.service.InventoryTransactionService;
import com.fernirx.sneakerapi.order.entity.Order;
import com.fernirx.sneakerapi.order.entity.OrderItem;
import com.fernirx.sneakerapi.order.enums.OrderStatus;
import com.fernirx.sneakerapi.order.service.OrderService;
import com.fernirx.sneakerapi.product.dto.response.StockChangeResult;
import com.fernirx.sneakerapi.product.entity.ProductVariant;
import com.fernirx.sneakerapi.product.service.ProductVariantService;
import com.fernirx.sneakerapi.returns.dto.request.CreateReturnRequest;
import com.fernirx.sneakerapi.returns.dto.request.ProcessReturnRequest;
import com.fernirx.sneakerapi.returns.dto.request.RejectReturnRequest;
import com.fernirx.sneakerapi.returns.dto.request.ReturnFilterRequest;
import com.fernirx.sneakerapi.returns.dto.request.ReturnItemRequest;
import com.fernirx.sneakerapi.returns.dto.request.UpdateTrackingRequest;
import com.fernirx.sneakerapi.returns.dto.response.EligibleOrderItemResponse;
import com.fernirx.sneakerapi.returns.dto.response.ReturnRequestInternalResponse;
import com.fernirx.sneakerapi.returns.dto.response.ReturnRequestItemResponse;
import com.fernirx.sneakerapi.returns.dto.response.ReturnRequestResponse;
import com.fernirx.sneakerapi.returns.entity.ReturnRequest;
import com.fernirx.sneakerapi.returns.entity.ReturnRequestImage;
import com.fernirx.sneakerapi.returns.entity.ReturnRequestItem;
import com.fernirx.sneakerapi.returns.enums.ReturnResolutionType;
import com.fernirx.sneakerapi.returns.enums.ReturnStatus;
import com.fernirx.sneakerapi.returns.mapper.ReturnRequestMapper;
import com.fernirx.sneakerapi.returns.repository.ReturnRequestImageRepository;
import com.fernirx.sneakerapi.returns.repository.ReturnRequestItemRepository;
import com.fernirx.sneakerapi.returns.repository.ReturnRequestRepository;
import com.fernirx.sneakerapi.returns.repository.ReturnRequestSpec;
import com.fernirx.sneakerapi.returns.service.ReturnRequestService;
import com.fernirx.sneakerapi.shipping.dto.ParcelItem;
import com.fernirx.sneakerapi.shipping.dto.command.CreateShipmentCommand;
import com.fernirx.sneakerapi.shipping.dto.response.ShipmentResult;
import com.fernirx.sneakerapi.shipping.service.ShippingService;
import com.fernirx.sneakerapi.user.entity.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Service
@Transactional
@RequiredArgsConstructor
public class ReturnRequestServiceImpl implements ReturnRequestService {

    private static final int RETURN_WINDOW_DAYS = 30;

    private final ReturnRequestRepository returnRequestRepository;
    private final ReturnRequestItemRepository returnRequestItemRepository;
    private final ReturnRequestImageRepository returnRequestImageRepository;
    private final ReturnRequestMapper returnRequestMapper;
    private final OrderService orderService;
    private final ProductVariantService productVariantService;
    private final InventoryTransactionService inventoryTransactionService;
    private final ShippingService shippingService;
    private final CustomerService customerService;
    private final PlatformTransactionManager transactionManager;

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @Transactional(readOnly = true)
    public List<EligibleOrderItemResponse> getEligibleItems(Long orderId, Long userId) {
        Order order = orderService.findOwnedEntityById(orderId, userId, null);
        if (order.getStatus() != OrderStatus.DELIVERED) {
            return List.of();
        }
        return orderService.findItemsByOrder(order).stream().map(this::toEligibleResponse).toList();
    }

    @Override
    public ReturnRequestResponse create(Long userId, CreateReturnRequest request) {
        Order order = orderService.findOwnedEntityById(request.orderId(), userId, null);
        if (order.getStatus() != OrderStatus.DELIVERED) {
            throw BusinessException.of(ErrorCode.RETURN_NOT_ELIGIBLE, "label.return_request");
        }
        LocalDateTime deliveredAt = orderService.findDeliveredAt(order.getId())
                .orElseThrow(() -> BusinessException.notFound("label.order"));
        if (deliveredAt.isBefore(LocalDateTime.now().minusDays(RETURN_WINDOW_DAYS))) {
            throw BusinessException.of(ErrorCode.RETURN_NOT_ELIGIBLE, "label.return_request");
        }

        ReturnRequest returnRequest = new ReturnRequest();
        returnRequest.setOrder(order);
        returnRequest.setCustomer(order.getCustomer());
        returnRequest.setCode(generateCode());
        returnRequest.setResolutionType(request.resolutionType());
        returnRequest.setStatus(ReturnStatus.PENDING);
        returnRequest.setReason(request.reason());
        returnRequest = returnRequestRepository.save(returnRequest);

        for (ReturnItemRequest itemRequest : request.items()) {
            returnRequestItemRepository.save(buildItem(returnRequest, order, request.resolutionType(), itemRequest));
        }

        if (!CollectionUtils.isEmpty(request.imagePublicIds())) {
            for (String publicId : request.imagePublicIds()) {
                ReturnRequestImage image = new ReturnRequestImage();
                image.setReturnRequest(returnRequest);
                image.setImagePublicId(publicId);
                returnRequestImageRepository.save(image);
            }
        }

        return buildResponse(returnRequest);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReturnRequestResponse> getMyReturns(Long userId, Long orderId, Pageable pageable) {
        Page<ReturnRequest> page = orderId != null
                ? returnRequestRepository.findByCustomer_User_IdAndOrder_IdOrderByCreatedAtDesc(userId, orderId, pageable)
                : returnRequestRepository.findByCustomer_User_IdOrderByCreatedAtDesc(userId, pageable);
        return page.map(this::buildResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public ReturnRequestResponse getMyReturnDetail(Long id, Long userId) {
        return buildResponse(findOwnedByUser(id, userId));
    }

    @Override
    public ReturnRequestResponse updateTracking(Long id, Long userId, UpdateTrackingRequest request) {
        ReturnRequest returnRequest = findOwnedByUser(id, userId);
        requireStatus(returnRequest, ReturnStatus.APPROVED);
        returnRequest.setTrackingCode(request.trackingCode());
        returnRequest = returnRequestRepository.save(returnRequest);
        return buildResponse(returnRequest);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReturnRequestInternalResponse> getAll(ReturnFilterRequest filter, Pageable pageable) {
        return returnRequestRepository.findAll(ReturnRequestSpec.build(filter), pageable)
                .map(this::buildInternalResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public ReturnRequestInternalResponse getById(Long id) {
        return buildInternalResponse(findById(id));
    }

    @Override
    public ReturnRequestInternalResponse approve(Long id, Long approvedByUserId) {
        ReturnRequest returnRequest = findById(id);
        requireStatus(returnRequest, ReturnStatus.PENDING);
        returnRequest.setStatus(ReturnStatus.APPROVED);
        returnRequest.setApprovedBy(approvedByUserId != null ? entityManager.getReference(User.class, approvedByUserId) : null);
        returnRequest.setApprovedAt(LocalDateTime.now());
        returnRequest = returnRequestRepository.save(returnRequest);
        return buildInternalResponse(returnRequest);
    }

    @Override
    public ReturnRequestInternalResponse reject(Long id, RejectReturnRequest request) {
        ReturnRequest returnRequest = findById(id);
        if (returnRequest.getStatus() != ReturnStatus.PENDING && returnRequest.getStatus() != ReturnStatus.APPROVED) {
            throw BusinessException.of(ErrorCode.RETURN_INVALID_STATUS, "label.return_request");
        }
        returnRequest.setStatus(ReturnStatus.REJECTED);
        returnRequest.setRejectReason(request.rejectReason());
        returnRequest = returnRequestRepository.save(returnRequest);
        return buildInternalResponse(returnRequest);
    }

    @Override
    public ReturnRequestInternalResponse markReceived(Long id) {
        ReturnRequest returnRequest = findById(id);
        requireStatus(returnRequest, ReturnStatus.APPROVED);
        returnRequest.setStatus(ReturnStatus.RECEIVED);
        returnRequest.setReceivedAt(LocalDateTime.now());
        returnRequest = returnRequestRepository.save(returnRequest);
        return buildInternalResponse(returnRequest);
    }

    @Override
    // Tách khỏi transaction lớp: nhánh EXCHANGE gọi GHN tạo vận đơn (I/O mạng, không thể rollback).
    // Nguyên tắc: mutation DB (trừ/tăng kho, set COMPLETED) LUÔN chạy và commit XONG trước, GHN chỉ được
    // gọi SAU KHI nghiệp vụ đã chắc chắn thành công - tránh việc GHN tạo vận đơn thật rồi mới phát hiện
    // hết hàng/lỗi DB (từng là root cause của race condition tạo vận đơn trùng khi retry).
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public ReturnRequestInternalResponse process(Long id, Long processedByUserId, ProcessReturnRequest request) {
        ReturnRequest returnRequest = findById(id);
        requireStatus(returnRequest, ReturnStatus.RECEIVED);

        if (Boolean.FALSE.equals(request.passed())) {
            runInNewTransaction(() -> {
                ReturnRequest managed = findById(id);
                managed.setStatus(ReturnStatus.REJECTED_AFTER_INSPECTION);
                managed.setRejectReason(request.rejectReason());
                managed.setAdminNote(request.adminNote());
                returnRequestRepository.save(managed);
            });
            return buildInternalResponseInNewTransaction(id);
        }

        // Bước 1+2: trừ/tăng kho + set COMPLETED, commit thật trong transaction riêng. Nếu hết hàng/lỗi
        // ở đây, GHN CHƯA từng được gọi - an toàn tuyệt đối, không có gì cần dọn dẹp.
        runInNewTransaction(() -> completeReturnWithoutShipment(id, processedByUserId, request));

        // Bước 3: chỉ sau khi bước trên đã commit, mới thử tạo vận đơn GHN cho nhánh EXCHANGE.
        ReturnRequest committed = findById(id);
        if (committed.getResolutionType() == ReturnResolutionType.EXCHANGE) {
            tryCreateExchangeShipment(committed);
        }

        return buildInternalResponseInNewTransaction(id);
    }

    @Override
    // Bước 4: retry thủ công (giống nguyên tắc đã chốt cho OrderServiceImpl.createShipment - lỗi thì để
    // admin bấm thử lại, không tự động). Chỉ cho phép khi COMPLETED + EXCHANGE + chưa có vận đơn.
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public ReturnRequestInternalResponse retryExchangeShipment(Long id) {
        ReturnRequest returnRequest = findById(id);
        if (returnRequest.getStatus() != ReturnStatus.COMPLETED
                || returnRequest.getResolutionType() != ReturnResolutionType.EXCHANGE
                || returnRequest.getExchangeShippingOrderCode() != null) {
            throw BusinessException.of(ErrorCode.RETURN_INVALID_STATUS, "label.return_request");
        }
        tryCreateExchangeShipment(returnRequest);
        return buildInternalResponseInNewTransaction(id);
    }

    // ---- Private helpers ----

    private void completeReturnWithoutShipment(Long id, Long processedByUserId, ProcessReturnRequest request) {
        ReturnRequest returnRequest = findById(id);
        List<ReturnRequestItem> items = returnRequestItemRepository.findAllByReturnRequest(returnRequest);

        BigDecimal totalRefund = BigDecimal.ZERO;
        for (ReturnRequestItem item : items) {
            Long variantId = item.getOrderItem().getVariant().getId();
            StockChangeResult stockChange = productVariantService.increaseStock(variantId, item.getQuantity());
            inventoryTransactionService.record(variantId, processedByUserId, InventoryTransactionType.IN, item.getQuantity(),
                    stockChange.oldStock(), stockChange.newStock(), InventoryReferenceType.RETURN, item.getId(),
                    "Hoàn kho từ yêu cầu đổi trả #" + returnRequest.getCode());

            if (returnRequest.getResolutionType() == ReturnResolutionType.EXCHANGE && item.getExchangeVariant() != null) {
                Long exchangeVariantId = item.getExchangeVariant().getId();
                StockChangeResult exchangeChange = productVariantService.decreaseStock(exchangeVariantId, item.getQuantity());
                inventoryTransactionService.record(exchangeVariantId, processedByUserId, InventoryTransactionType.OUT, item.getQuantity(),
                        exchangeChange.oldStock(), exchangeChange.newStock(), InventoryReferenceType.RETURN, item.getId(),
                        "Xuất hàng đổi cho yêu cầu đổi trả #" + returnRequest.getCode());
            }
            totalRefund = totalRefund.add(item.getRefundAmount());
        }

        returnRequest.setStatus(ReturnStatus.COMPLETED);
        returnRequest.setCompletedAt(LocalDateTime.now());
        returnRequest.setAdminNote(request.adminNote());

        if (returnRequest.getResolutionType() == ReturnResolutionType.REFUND) {
            returnRequest.setRefundAmount(totalRefund);
            returnRequest.setRefundedAt(LocalDateTime.now());
            // Đổi hàng (EXCHANGE) không đổi tổng chi tiêu của khách nên KHÔNG thu hồi điểm loyalty - chỉ REFUND mới gọi.
            customerService.revokePartial(returnRequest.getCustomer().getId(), returnRequest.getOrder().getId(),
                    returnRequest.getId(), totalRefund);
        }

        returnRequestRepository.save(returnRequest);
    }

    // Gọi GHN tạo vận đơn cho hàng đổi - LUÔN chạy sau khi completeReturnWithoutShipment đã commit.
    // Idempotent: nếu đã có exchangeShippingOrderCode thì bỏ qua, không gọi GHN lại (khớp pattern
    // OrderServiceImpl.createShipment). Lỗi GHN không ném ngược lên caller - return request đã COMPLETED
    // hợp lệ, chỉ còn khâu vận chuyển vật lý; lý do lỗi được ghi vào adminNote để admin thấy và tự bấm
    // "thử lại" (retryExchangeShipment) sau, đúng nguyên tắc "lỗi thì để admin retry" đã áp dụng cho
    // module shipping gốc.
    private void tryCreateExchangeShipment(ReturnRequest returnRequest) {
        if (returnRequest.getExchangeShippingOrderCode() != null) {
            return;
        }
        List<ReturnRequestItem> items = returnRequestItemRepository.findAllByReturnRequest(returnRequest);
        List<ParcelItem> parcelItems = items.stream()
                .filter(item -> item.getExchangeVariant() != null)
                .map(item -> ParcelItem.from(item.getExchangeVariant(), item.getQuantity()))
                .toList();
        Order order = returnRequest.getOrder();
        CreateShipmentCommand command = new CreateShipmentCommand(
                order.getRecipientName(), order.getRecipientPhone(),
                order.getShippingStreet(), order.getShippingWard(), order.getShippingDistrict(), order.getShippingProvince(),
                returnRequest.getCode(), 0L, 1,
                "Gửi hàng đổi cho yêu cầu đổi trả #" + returnRequest.getCode(),
                parcelItems
        );

        Long returnRequestId = returnRequest.getId();
        try {
            ShipmentResult result = shippingService.createShipment(command);
            runInNewTransaction(() -> {
                ReturnRequest managed = findById(returnRequestId);
                managed.setExchangeShippingOrderCode(result.shippingOrderCode());
                managed.setExchangeExpectedDeliveryAt(result.expectedDeliveryAt());
                returnRequestRepository.save(managed);
            });
        } catch (Exception e) {
            String failureNote = "[Tạo vận đơn GHN thất bại, cần bấm thử lại] " + e.getMessage();
            runInNewTransaction(() -> {
                ReturnRequest managed = findById(returnRequestId);
                String existingNote = managed.getAdminNote();
                managed.setAdminNote(existingNote != null ? existingNote + "\n" + failureNote : failureNote);
                returnRequestRepository.save(managed);
            });
        }
    }

    private ReturnRequestItem buildItem(ReturnRequest returnRequest, Order order, ReturnResolutionType resolutionType,
                                         ReturnItemRequest itemRequest) {
        OrderItem orderItem = orderService.findItemById(itemRequest.orderItemId());
        if (!orderItem.getOrder().getId().equals(order.getId())) {
            throw BusinessException.of(ErrorCode.RETURN_QUANTITY_EXCEEDED, "label.order.item");
        }

        int alreadyRequested = returnRequestItemRepository.sumQuantityByOrderItemExcludingRejected(orderItem.getId());
        if (alreadyRequested + itemRequest.quantity() > orderItem.getQuantity()) {
            throw BusinessException.of(ErrorCode.RETURN_QUANTITY_EXCEEDED, "label.quantity");
        }

        ReturnRequestItem item = new ReturnRequestItem();
        item.setReturnRequest(returnRequest);
        item.setOrderItem(orderItem);
        item.setQuantity(itemRequest.quantity());

        if (resolutionType == ReturnResolutionType.EXCHANGE) {
            item.setExchangeVariant(resolveExchangeVariant(orderItem, itemRequest.exchangeVariantId()));
        }

        item.setRefundAmount(orderItem.getUnitPrice().multiply(BigDecimal.valueOf(itemRequest.quantity())));
        return item;
    }

    private ProductVariant resolveExchangeVariant(OrderItem orderItem, Long exchangeVariantId) {
        if (exchangeVariantId == null) {
            throw BusinessException.of(ErrorCode.RETURN_VARIANT_INVALID, "label.variantId");
        }
        ProductVariant original = orderItem.getVariant();
        ProductVariant exchangeVariant = productVariantService.findActiveById(exchangeVariantId);

        if (exchangeVariant.getId().equals(original.getId())
                || !exchangeVariant.getProduct().getId().equals(original.getProduct().getId())) {
            throw BusinessException.of(ErrorCode.RETURN_VARIANT_INVALID, "label.variantId");
        }

        // v1 chỉ cho đổi cùng giá - tránh phải xử lý thu thêm/hoàn thêm chênh lệch (xem plan)
        BigDecimal originalPrice = original.getPrice() != null ? original.getPrice() : orderItem.getUnitPrice();
        if (exchangeVariant.getPrice() == null || originalPrice == null
                || exchangeVariant.getPrice().compareTo(originalPrice) != 0) {
            throw BusinessException.of(ErrorCode.RETURN_VARIANT_INVALID, "label.variantId");
        }
        return exchangeVariant;
    }

    private EligibleOrderItemResponse toEligibleResponse(OrderItem orderItem) {
        int alreadyRequested = returnRequestItemRepository.sumQuantityByOrderItemExcludingRejected(orderItem.getId());
        int maxReturnable = Math.max(0, orderItem.getQuantity() - alreadyRequested);

        ProductVariant original = orderItem.getVariant();
        BigDecimal originalPrice = original.getPrice() != null ? original.getPrice() : orderItem.getUnitPrice();

        List<EligibleOrderItemResponse.ExchangeCandidate> candidates = productVariantService
                .getVariants(original.getProduct().getId()).stream()
                .flatMap(group -> group.variants().stream()
                        .filter(v -> !v.id().equals(original.getId()))
                        .filter(v -> Boolean.TRUE.equals(v.active()))
                        .filter(v -> v.price() != null && originalPrice != null && v.price().compareTo(originalPrice) == 0)
                        .map(v -> new EligibleOrderItemResponse.ExchangeCandidate(
                                v.id(), v.size(), v.shoeWidth(), group.colorway(), v.sku(), v.price(), v.stockQuantity())))
                .toList();

        return new EligibleOrderItemResponse(
                orderItem.getId(), orderItem.getProductName(), orderItem.getVariantSku(),
                orderItem.getVariantSize(), orderItem.getVariantColor(), orderItem.getUnitPrice(),
                orderItem.getQuantity(), maxReturnable, candidates
        );
    }

    private void requireStatus(ReturnRequest returnRequest, ReturnStatus expected) {
        if (returnRequest.getStatus() != expected) {
            throw BusinessException.of(ErrorCode.RETURN_INVALID_STATUS, "label.return_request");
        }
    }

    private String generateCode() {
        return "RET" + System.currentTimeMillis() + ThreadLocalRandom.current().nextInt(100, 999);
    }

    private List<ReturnRequestItemResponse> mapItems(ReturnRequest returnRequest) {
        return returnRequestItemRepository.findAllByReturnRequest(returnRequest).stream()
                .map(returnRequestMapper::toItemResponse).toList();
    }

    private List<String> mapImages(ReturnRequest returnRequest) {
        return returnRequestImageRepository.findAllByReturnRequest(returnRequest).stream()
                .map(ReturnRequestImage::getImagePublicId).toList();
    }

    private ReturnRequestResponse buildResponse(ReturnRequest returnRequest) {
        return returnRequestMapper.toResponse(returnRequest, mapItems(returnRequest), mapImages(returnRequest));
    }

    private ReturnRequestInternalResponse buildInternalResponse(ReturnRequest returnRequest) {
        return returnRequestMapper.toInternalResponse(returnRequest, mapItems(returnRequest), mapImages(returnRequest));
    }

    /** Dùng sau khi chạy trong method NOT_SUPPORTED (process) - cần mở transaction thật để lazy-load an toàn, giống OrderServiceImpl.buildInternalResponse. */
    private ReturnRequestInternalResponse buildInternalResponseInNewTransaction(Long id) {
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        transactionTemplate.setReadOnly(true);
        return transactionTemplate.execute(status -> buildInternalResponse(findById(id)));
    }

    private void runInNewTransaction(Runnable action) {
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        transactionTemplate.executeWithoutResult(status -> action.run());
    }

    private ReturnRequest findById(Long id) {
        return returnRequestRepository.findById(id)
                .orElseThrow(() -> BusinessException.notFound("label.return_request"));
    }

    private ReturnRequest findOwnedByUser(Long id, Long userId) {
        return returnRequestRepository.findByIdAndCustomer_User_Id(id, userId)
                .orElseThrow(() -> BusinessException.notFound("label.return_request"));
    }
}
