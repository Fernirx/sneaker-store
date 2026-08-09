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
import com.fernirx.sneakerapi.notification.event.ReturnRequestApprovedEvent;
import com.fernirx.sneakerapi.notification.event.ReturnRequestCompletedEvent;
import com.fernirx.sneakerapi.notification.event.ReturnRequestCreatedEvent;
import com.fernirx.sneakerapi.notification.event.ReturnRequestInspectionFailedEvent;
import com.fernirx.sneakerapi.notification.event.ReturnRequestRejectedEvent;
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
import org.springframework.context.ApplicationEventPublisher;
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
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

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
    private final ApplicationEventPublisher eventPublisher;

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * Lấy danh sách các sản phẩm đủ điều kiện đổi/trả trong một đơn hàng.
     * Quy tắc: Đơn hàng phải thuộc về User và phải ở trạng thái DELIVERED.
     */
    @Override
    @Transactional(readOnly = true)
    public List<EligibleOrderItemResponse> getEligibleItems(Long orderId, Long userId) {
        Order order = orderService.findOwnedEntityById(orderId, userId, null);
        if (order.getStatus() != OrderStatus.DELIVERED) {
            return List.of();
        }
        return orderService.findItemsByOrder(order).stream().map(this::toEligibleResponse).toList();
    }

    /**
     * Khách hàng tạo yêu cầu đổi/trả.
     * Luồng xử lý:
     * 1. Xác thực đơn hàng phải là DELIVERED và còn trong thời hạn (30 ngày).
     * 2. Lưu thông tin ReturnRequest và các mặt hàng (ReturnRequestItem).
     * 3. Lưu hình ảnh minh chứng (nếu có).
     * 4. Bắn sự kiện ReturnRequestCreatedEvent.
     */
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

        eventPublisher.publishEvent(new ReturnRequestCreatedEvent(returnRequest.getId(), returnRequest.getCode()));

        return buildResponse(returnRequest);
    }

    /**
     * Lấy danh sách lịch sử đổi/trả của một User (dành cho Customer).
     */
    @Override
    @Transactional(readOnly = true)
    public Page<ReturnRequestResponse> getMyReturns(Long userId, Long orderId, Pageable pageable) {
        Page<ReturnRequest> page = orderId != null
                ? returnRequestRepository.findByCustomer_User_IdAndOrder_IdOrderByCreatedAtDesc(userId, orderId, pageable)
                : returnRequestRepository.findByCustomer_User_IdOrderByCreatedAtDesc(userId, pageable);
        return page.map(this::buildResponse);
    }

    /**
     * Lấy chi tiết yêu cầu đổi/trả của một User.
     */
    @Override
    @Transactional(readOnly = true)
    public ReturnRequestResponse getMyReturnDetail(Long id, Long userId) {
        return buildResponse(findOwnedByUser(id, userId));
    }

    /**
     * Khách hàng cập nhật mã vận đơn (tracking code) sau khi đã tự gửi hàng trả lại.
     * Chỉ được cập nhật khi yêu cầu đang ở trạng thái APPROVED.
     */
    @Override
    public ReturnRequestResponse updateTracking(Long id, Long userId, UpdateTrackingRequest request) {
        ReturnRequest returnRequest = findOwnedByUser(id, userId);
        requireStatus(returnRequest, ReturnStatus.APPROVED);
        returnRequest.setTrackingCode(request.trackingCode());
        returnRequest = returnRequestRepository.save(returnRequest);
        return buildResponse(returnRequest);
    }

    /**
     * Lấy danh sách tất cả các yêu cầu đổi/trả (dành cho Admin/Staff).
     */
    @Override
    @Transactional(readOnly = true)
    public Page<ReturnRequestInternalResponse> getAll(ReturnFilterRequest filter, Pageable pageable) {
        return returnRequestRepository.findAll(ReturnRequestSpec.build(filter), pageable)
                .map(this::buildInternalResponse);
    }

    /**
     * Lấy chi tiết một yêu cầu đổi/trả (dành cho Admin/Staff).
     */
    @Override
    @Transactional(readOnly = true)
    public ReturnRequestInternalResponse getById(Long id) {
        return buildInternalResponse(findById(id));
    }

    /**
     * Admin duyệt yêu cầu đổi/trả (chuyển từ PENDING -> APPROVED).
     * Nếu là loại EXCHANGE (Đổi hàng), hệ thống sẽ check Advisory Stock (validateExchangeStock)
     * để cảnh báo nếu mặt hàng đổi không đủ tồn kho.
     */
    @Override
    public ReturnRequestInternalResponse approve(Long id, Long approvedByUserId) {
        ReturnRequest returnRequest = findById(id);
        requireStatus(returnRequest, ReturnStatus.PENDING);
        if (returnRequest.getResolutionType() == ReturnResolutionType.EXCHANGE) {
            validateExchangeStock(returnRequest);
        }
        returnRequest.setStatus(ReturnStatus.APPROVED);
        returnRequest.setApprovedBy(approvedByUserId != null ? entityManager.getReference(User.class, approvedByUserId) : null);
        returnRequest.setApprovedAt(LocalDateTime.now());
        returnRequest = returnRequestRepository.save(returnRequest);
        eventPublisher.publishEvent(new ReturnRequestApprovedEvent(
                returnRequest.getId(), returnRequest.getCode(), returnRequest.getCustomer().getUser().getId()));
        return buildInternalResponse(returnRequest);
    }

    /**
     * Admin từ chối yêu cầu đổi/trả.
     * Được phép từ chối khi đang PENDING hoặc đã APPROVED (khách gửi hàng nhưng không đúng).
     */
    @Override
    public ReturnRequestInternalResponse reject(Long id, RejectReturnRequest request) {
        ReturnRequest returnRequest = findById(id);
        if (returnRequest.getStatus() != ReturnStatus.PENDING && returnRequest.getStatus() != ReturnStatus.APPROVED) {
            throw BusinessException.of(ErrorCode.RETURN_INVALID_STATUS, "label.return_request");
        }
        returnRequest.setStatus(ReturnStatus.REJECTED);
        returnRequest.setRejectReason(request.rejectReason());
        returnRequest = returnRequestRepository.save(returnRequest);
        eventPublisher.publishEvent(new ReturnRequestRejectedEvent(
                returnRequest.getId(), returnRequest.getCode(), request.rejectReason(), returnRequest.getCustomer().getUser().getId()));
        return buildInternalResponse(returnRequest);
    }

    /**
     * Kho xác nhận đã nhận được hàng từ khách gửi trả (APPROVED -> RECEIVED).
     */
    @Override
    public ReturnRequestInternalResponse markReceived(Long id) {
        ReturnRequest returnRequest = findById(id);
        requireStatus(returnRequest, ReturnStatus.APPROVED);
        returnRequest.setStatus(ReturnStatus.RECEIVED);
        returnRequest.setReceivedAt(LocalDateTime.now());
        returnRequest = returnRequestRepository.save(returnRequest);
        return buildInternalResponse(returnRequest);
    }

    /**
     * Xử lý bước cuối cùng: Kiểm tra chất lượng hàng hoàn và Hoàn tiền / Lên đơn giao hàng đổi.
     * Tách khỏi transaction lớp: nhánh EXCHANGE gọi GHN tạo vận đơn (I/O mạng, không thể rollback).
     * Nguyên tắc: mutation DB (trừ/tăng kho, set COMPLETED) LUÔN chạy và commit XONG trước, GHN chỉ được
     * gọi SAU KHI nghiệp vụ đã chắc chắn thành công - tránh việc GHN tạo vận đơn thật rồi mới phát hiện
     * hết hàng/lỗi DB (từng là root cause của race condition tạo vận đơn trùng khi retry).
     */
    @Override
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
                // Publish trong cùng block - đảm bảo AFTER_COMMIT gắn đúng vào transaction này (mở qua
                // TransactionTemplate ở runInNewTransaction), không phải tạo notification trước khi
                // ReturnRequest thật sự commit.
                eventPublisher.publishEvent(new ReturnRequestInspectionFailedEvent(
                        managed.getId(), managed.getCode(), request.rejectReason(), managed.getCustomer().getUser().getId()));
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

    /**
     * Admin xác nhận đã chuyển tiền hoàn lại cho khách hàng (Nhánh REFUND).
     * Chuyển trạng thái từ REFUND_PENDING -> COMPLETED, thu hồi điểm thưởng và bắn event.
     */
    @Override
    public ReturnRequestInternalResponse markAsRefunded(Long id, Long refundedByUserId) {
        ReturnRequest returnRequest = findById(id);
        requireStatus(returnRequest, ReturnStatus.REFUND_PENDING);

        returnRequest.setStatus(ReturnStatus.COMPLETED);
        returnRequest.setRefundedAt(LocalDateTime.now());
        returnRequest.setCompletedAt(LocalDateTime.now());

        customerService.revokePartial(returnRequest.getCustomer().getId(), returnRequest.getOrder().getId(),
                returnRequest.getId(), returnRequest.getRefundAmount());

        returnRequest = returnRequestRepository.save(returnRequest);

        eventPublisher.publishEvent(new ReturnRequestCompletedEvent(
                returnRequest.getId(), returnRequest.getCode(), returnRequest.getCustomer().getUser().getId(), true));

        return buildInternalResponse(returnRequest);
    }

    /**
     * Admin gọi retry thử tạo lại vận đơn GHN nếu bước process trước đó gọi GHN bị lỗi.
     * Bước 4: retry thủ công (giống nguyên tắc đã chốt cho OrderServiceImpl.createShipment - lỗi thì để
     * admin bấm thử lại, không tự động). Chỉ cho phép khi COMPLETED + EXCHANGE + chưa có vận đơn.
     */
    @Override
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

    /**
     * Hoàn thành nghiệp vụ đổi/trả mà KHÔNG kèm phần gọi mạng (GHN).
     * Nhập kho hàng hoàn về, xuất kho hàng đổi đi (nếu có), hoàn tiền cho khách, và đổi trạng thái thành COMPLETED.
     */
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

        returnRequest.setAdminNote(request.adminNote());

        if (returnRequest.getResolutionType() == ReturnResolutionType.EXCHANGE) {
            returnRequest.setStatus(ReturnStatus.COMPLETED);
            returnRequest.setCompletedAt(LocalDateTime.now());
            returnRequestRepository.save(returnRequest);
            eventPublisher.publishEvent(new ReturnRequestCompletedEvent(
                    returnRequest.getId(), returnRequest.getCode(), returnRequest.getCustomer().getUser().getId(), false));
        } else {
            returnRequest.setStatus(ReturnStatus.REFUND_PENDING);
            returnRequest.setRefundAmount(totalRefund); // Lưu số tiền cần hoàn để hiển thị cho UI
            returnRequestRepository.save(returnRequest);
        }
    }

    /**
     * Gọi GHN tạo vận đơn cho hàng đổi - LUÔN chạy sau khi completeReturnWithoutShipment đã commit.
     * Idempotent: nếu đã có exchangeShippingOrderCode thì bỏ qua, không gọi GHN lại (khớp pattern
     * OrderServiceImpl.createShipment). Lỗi GHN không ném ngược lên caller - return request đã COMPLETED
     * hợp lệ, chỉ còn khâu vận chuyển vật lý; lý do lỗi được ghi vào adminNote để admin thấy và tự bấm
     * "thử lại" (retryExchangeShipment) sau, đúng nguyên tắc "lỗi thì để admin retry" đã áp dụng cho
     * module shipping gốc.
     */
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

    /**
     * Build đối tượng ReturnRequestItem từ request của khách hàng.
     * Kiểm tra chặt chẽ số lượng trả không được vượt quá số lượng mua thực tế (đã trừ đi những lần đổi trả trước).
     */
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

    /**
     * Xác định và xác thực biến thể khách muốn đổi (Exchange Variant).
     * Quy tắc: Phải cùng dòng sản phẩm, cùng giá trị để tránh rắc rối thu/hoàn tiền chênh lệch.
     */
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

    /**
     * Map OrderItem sang EligibleOrderItemResponse để hiển thị cho khách những món hàng có thể trả.
     */
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

    /**
     * Advisory check - chỉ hỗ trợ SALE quyết định sớm (trước khi khách gửi trả hàng gốc đi), KHÔNG phải
     * cơ chế giữ/reserve hàng. Tồn kho vẫn có thể đổi giữa lúc duyệt và lúc WAREHOUSE process() - decreaseStock()
     * atomic ở completeReturnWithoutShipment() mới là lớp kiểm soát cuối cùng, không đổi gì ở luồng đó.
     */
    private void validateExchangeStock(ReturnRequest returnRequest) {
        List<ReturnRequestItem> items = returnRequestItemRepository.findAllByReturnRequest(returnRequest);
        // Gom theo exchangeVariant trước khi so với tồn kho - nhiều dòng trong cùng request có thể cùng
        // chọn đổi sang 1 variant, check riêng lẻ từng dòng sẽ bỏ sót trường hợp cộng dồn vượt quá tồn kho.
        Map<Long, Integer> requestedByVariantId = items.stream()
                .filter(item -> item.getExchangeVariant() != null)
                .collect(Collectors.groupingBy(item -> item.getExchangeVariant().getId(), Collectors.summingInt(ReturnRequestItem::getQuantity)));
        for (Map.Entry<Long, Integer> entry : requestedByVariantId.entrySet()) {
            ProductVariant variant = productVariantService.findActiveById(entry.getKey());
            if (variant.getStockQuantity() < entry.getValue()) {
                throw BusinessException.bad("label.product.stock");
            }
        }
    }

    /**
     * Helper kiểm tra trạng thái của ReturnRequest phải khớp với mong đợi.
     */
    private void requireStatus(ReturnRequest returnRequest, ReturnStatus expected) {
        if (returnRequest.getStatus() != expected) {
            throw BusinessException.of(ErrorCode.RETURN_INVALID_STATUS, "label.return_request");
        }
    }

    /**
     * Sinh mã hoàn trả (Code) duy nhất.
     */
    private String generateCode() {
        return "RET" + System.currentTimeMillis() + ThreadLocalRandom.current().nextInt(100, 999);
    }

    /**
     * Helper map danh sách Item sang Response.
     */
    private List<ReturnRequestItemResponse> mapItems(ReturnRequest returnRequest) {
        return returnRequestItemRepository.findAllByReturnRequest(returnRequest).stream()
                .map(returnRequestMapper::toItemResponse).toList();
    }

    /**
     * Helper map danh sách ảnh minh chứng sang Response.
     */
    private List<String> mapImages(ReturnRequest returnRequest) {
        return returnRequestImageRepository.findAllByReturnRequest(returnRequest).stream()
                .map(ReturnRequestImage::getImagePublicId).toList();
    }

    /**
     * Xây dựng DTO Response cho Storefront (ẩn các note nội bộ của admin).
     */
    private ReturnRequestResponse buildResponse(ReturnRequest returnRequest) {
        return returnRequestMapper.toResponse(returnRequest, mapItems(returnRequest), mapImages(returnRequest));
    }

    /**
     * Xây dựng DTO Internal Response cho CMS (hiển thị đầy đủ note nội bộ).
     */
    private ReturnRequestInternalResponse buildInternalResponse(ReturnRequest returnRequest) {
        return returnRequestMapper.toInternalResponse(returnRequest, mapItems(returnRequest), mapImages(returnRequest));
    }

    /**
     * Dùng sau khi chạy trong method NOT_SUPPORTED (process) - cần mở transaction thật để lazy-load an toàn, giống OrderServiceImpl.buildInternalResponse. 
     */
    private ReturnRequestInternalResponse buildInternalResponseInNewTransaction(Long id) {
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        transactionTemplate.setReadOnly(true);
        return transactionTemplate.execute(status -> buildInternalResponse(findById(id)));
    }

    /**
     * Khởi tạo một Transaction mới hoàn toàn (REQUIRES_NEW) để chạy action.
     * Đảm bảo DB được commit ngay lập tức bất chấp transaction bọc ngoài (NOT_SUPPORTED).
     */
    private void runInNewTransaction(Runnable action) {
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        transactionTemplate.executeWithoutResult(status -> action.run());
    }

    /**
     * Tìm ReturnRequest theo ID.
     */
    private ReturnRequest findById(Long id) {
        return returnRequestRepository.findById(id)
                .orElseThrow(() -> BusinessException.notFound("label.return_request"));
    }

    /**
     * Tìm ReturnRequest của đúng một User, chống việc xem trộm của user khác.
     */
    private ReturnRequest findOwnedByUser(Long id, Long userId) {
        return returnRequestRepository.findByIdAndCustomer_User_Id(id, userId)
                .orElseThrow(() -> BusinessException.notFound("label.return_request"));
    }
}
