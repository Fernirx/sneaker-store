package com.fernirx.sneakerapi.order.service.impl;

import com.fernirx.sneakerapi.cart.dto.response.CartItemResponse;
import com.fernirx.sneakerapi.cart.dto.response.CartResponse;
import com.fernirx.sneakerapi.cart.service.CartService;
import com.fernirx.sneakerapi.common.enums.ErrorCode;
import com.fernirx.sneakerapi.common.exception.BusinessException;
import com.fernirx.sneakerapi.coupon.dto.response.CouponApplyResult;
import com.fernirx.sneakerapi.coupon.service.CouponService;
import com.fernirx.sneakerapi.customer.entity.Customer;
import com.fernirx.sneakerapi.customer.enums.MembershipTier;
import com.fernirx.sneakerapi.customer.service.CustomerService;
import com.fernirx.sneakerapi.inventory.enums.InventoryReferenceType;
import com.fernirx.sneakerapi.inventory.enums.InventoryTransactionType;
import com.fernirx.sneakerapi.inventory.service.InventoryTransactionService;
import com.fernirx.sneakerapi.notification.event.OrderCancelledEvent;
import com.fernirx.sneakerapi.notification.event.OrderCreatedEvent;
import com.fernirx.sneakerapi.order.config.OrderProperties;
import com.fernirx.sneakerapi.setting.dto.response.StoreSettingResponse;
import com.fernirx.sneakerapi.setting.service.SettingService;
import com.fernirx.sneakerapi.order.dto.request.CreateOrderRequest;
import com.fernirx.sneakerapi.order.dto.request.OrderFilterRequest;
import com.fernirx.sneakerapi.order.dto.request.UpdateOrderStatusRequest;
import com.fernirx.sneakerapi.order.dto.response.OrderInternalResponse;
import com.fernirx.sneakerapi.order.dto.response.OrderItemResponse;
import com.fernirx.sneakerapi.order.dto.response.OrderResponse;
import com.fernirx.sneakerapi.order.dto.response.OrderStatusHistoryResponse;
import com.fernirx.sneakerapi.order.entity.Order;
import com.fernirx.sneakerapi.order.entity.OrderItem;
import com.fernirx.sneakerapi.order.entity.OrderStatusHistory;
import com.fernirx.sneakerapi.order.enums.OrderPaymentStatus;
import com.fernirx.sneakerapi.order.enums.OrderStatus;
import com.fernirx.sneakerapi.order.enums.PaymentMethod;
import com.fernirx.sneakerapi.order.mapper.OrderMapper;
import com.fernirx.sneakerapi.order.repository.OrderItemRepository;
import com.fernirx.sneakerapi.order.repository.OrderRepository;
import com.fernirx.sneakerapi.order.repository.OrderSpec;
import com.fernirx.sneakerapi.order.repository.OrderStatusHistoryRepository;
import com.fernirx.sneakerapi.order.service.OrderService;
import com.fernirx.sneakerapi.product.dto.response.StockChangeResult;
import com.fernirx.sneakerapi.product.entity.ProductVariant;
import com.fernirx.sneakerapi.product.service.ProductVariantService;
import com.fernirx.sneakerapi.shipping.dto.ParcelItem;
import com.fernirx.sneakerapi.shipping.dto.command.CalculateShippingFeeCommand;
import com.fernirx.sneakerapi.shipping.dto.command.CreateShipmentCommand;
import com.fernirx.sneakerapi.shipping.dto.response.ShipmentResult;
import com.fernirx.sneakerapi.shipping.dto.response.ShipmentStatusResult;
import com.fernirx.sneakerapi.shipping.entity.Shipment;
import com.fernirx.sneakerapi.shipping.repository.ShipmentRepository;
import com.fernirx.sneakerapi.shipping.service.ShippingService;
import com.fernirx.sneakerapi.user.entity.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {
    private static final String GHN_CANCEL_STATUS = "cancel";
    private static final String GHN_DELIVERED_STATUS = "delivered";

    private static final Map<OrderStatus, Set<OrderStatus>> ALLOWED_TRANSITIONS = new EnumMap<>(OrderStatus.class);
    static {
        ALLOWED_TRANSITIONS.put(OrderStatus.PENDING, EnumSet.of(OrderStatus.CONFIRMED, OrderStatus.CANCELLED));
        ALLOWED_TRANSITIONS.put(OrderStatus.CONFIRMED, EnumSet.of(OrderStatus.SHIPPING, OrderStatus.CANCELLED));
        ALLOWED_TRANSITIONS.put(OrderStatus.SHIPPING, EnumSet.of(OrderStatus.DELIVERED, OrderStatus.CANCELLED, OrderStatus.CONFIRMED));
        ALLOWED_TRANSITIONS.put(OrderStatus.DELIVERED, EnumSet.noneOf(OrderStatus.class));
        ALLOWED_TRANSITIONS.put(OrderStatus.CANCELLED, EnumSet.noneOf(OrderStatus.class));
    }

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderStatusHistoryRepository orderStatusHistoryRepository;
    private final OrderMapper orderMapper;
    private final OrderProperties orderProperties;
    private final CartService cartService;
    private final CustomerService customerService;
    private final ProductVariantService productVariantService;
    private final CouponService couponService;
    private final InventoryTransactionService inventoryTransactionService;
    private final ShippingService shippingService;
    private final ShipmentRepository shipmentRepository;
    private final PlatformTransactionManager transactionManager;
    private final ApplicationEventPublisher eventPublisher;
    private final SettingService settingService;

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * Tạo đơn hàng mới từ giỏ hàng.
     * Luồng xử lý:
     * 1. Kiểm tra header Idempotency-Key bắt buộc. Nếu đã tồn tại đơn hàng với key này (do double-click hoặc retry mạng), trả về đơn cũ ngay lập tức.
     * 2. Xác thực danh tính:
     *    - Nếu là Guest: Yêu cầu phải có email và mã OTP hợp lệ.
     *    - Nếu là User: Lấy email từ Customer entity.
     * 3. Kiểm tra giỏ hàng: Phải có ít nhất 1 sản phẩm đang được chọn.
     * 4. Tính toán chi phí: Tổng tiền hàng, phí ship (từ API GHN), trừ tiền mã giảm giá (nếu có nhập và hợp lệ).
     * 5. Lưu thông tin Order xuống DB. Bắt lỗi Unique Constraint (nếu 2 request lọt qua bước 1 cùng lúc) để chặn tạo đơn trùng.
     * 6. Lưu thông tin các món hàng (OrderItem), đồng thời gọi service trừ tồn kho thực tế.
     * 7. Khởi tạo lịch sử đơn hàng (OrderStatusHistory) là PENDING.
     * 8. Ghi nhận đã sử dụng coupon, xóa các món đã mua khỏi giỏ, và bắn sự kiện OrderCreatedEvent.
     */
    @Override
    public OrderResponse createOrder(Long userId, String guestToken, String idempotencyKey, CreateOrderRequest request) {
        if (!StringUtils.hasText(idempotencyKey)) {
            throw BusinessException.bad("label.idempotency_key");
        }

        Optional<Order> existingOrder = orderRepository.findByIdempotencyKey(idempotencyKey);
        if (existingOrder.isPresent()) {
            Order existing = existingOrder.get();
            return orderMapper.toResponse(existing, mapItems(existing), findShipment(existing));
        }

        if (StringUtils.hasText(request.hpAddress()) ||
            StringUtils.hasText(request.hpPhone()) ||
            StringUtils.hasText(request.hpEmail())) {
            throw BusinessException.bad("label.info");
        }

        boolean isGuest = (userId == null);
        String email = resolveCustomerEmail(userId, request, isGuest);
        Customer customer = isGuest ? null : customerService.getOrCreateByUserId(userId);

        List<ResolvedItem> resolvedItems = resolveCartItems(userId, guestToken);
        OrderPricing pricing = calculateOrderPricing(request, resolvedItems, email, customer);

        Order order = saveOrderEntity(customer, guestToken, idempotencyKey, request, pricing, isGuest);
        List<OrderItem> savedItems = processOrderItems(order, resolvedItems, userId);
        
        processPostOrderCreation(order, pricing, email, request, isGuest, guestToken, userId);

        List<OrderItemResponse> itemResponses = savedItems.stream().map(orderMapper::toItemResponse).toList();
        return orderMapper.toResponse(order, itemResponses, null);
    }

    /**
     * Lấy danh sách đơn hàng của khách hàng hiện tại (có phân trang).
     */
    @Override
    @Transactional(readOnly = true)
    public Page<OrderResponse> getMyOrders(Long userId, String guestToken, Pageable pageable) {
        requireIdentity(userId, guestToken);
        Page<Order> orders = userId != null
                ? orderRepository.findByCustomer_User_Id(userId, pageable)
                : orderRepository.findByGuestToken(guestToken, pageable);
        Map<Long, Shipment> shipmentsByOrderId = findShipmentsByOrders(orders.getContent());
        return orders.map(order -> orderMapper.toResponse(order, mapItems(order), shipmentsByOrderId.get(order.getId())));
    }

    /**
     * Lấy chi tiết đơn hàng của khách hàng hiện tại.
     */
    @Override
    @Transactional(readOnly = true)
    public OrderResponse getMyOrderDetail(Long orderId, Long userId, String guestToken) {
        Order order = findOwnedOrder(orderId, userId, guestToken);
        return orderMapper.toResponse(order, mapItems(order), findShipment(order));
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse trackOrder(String trackingToken) {
        Order order = orderRepository.findByTrackingToken(trackingToken)
                .orElseThrow(() -> BusinessException.notFound("label.order"));
        return orderMapper.toResponse(order, mapItems(order), findShipment(order));
    }

    /**
     * Lấy lịch sử chuyển trạng thái của một đơn hàng của khách.
     */
    @Override
    @Transactional(readOnly = true)
    public List<OrderStatusHistoryResponse> getMyOrderHistory(Long orderId, Long userId, String guestToken) {
        Order order = findOwnedOrder(orderId, userId, guestToken);
        return orderStatusHistoryRepository.findByOrderOrderByCreatedAtAsc(order).stream()
                .map(orderMapper::toHistoryResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderStatusHistoryResponse> trackOrderHistory(String trackingToken) {
        Order order = orderRepository.findByTrackingToken(trackingToken)
                .orElseThrow(() -> BusinessException.notFound("label.order"));
        return orderStatusHistoryRepository.findByOrderOrderByCreatedAtAsc(order).stream()
                .map(orderMapper::toHistoryResponse)
                .toList();
    }

    /**
     * Khách hàng tự hủy đơn hàng.
     * Điều kiện kiểm tra:
     * - Chỉ cho phép hủy nếu đơn hàng đang ở trạng thái PENDING. Nếu đã xác nhận hoặc giao hàng thì ném ngoại lệ.
     */
    @Override
    public void customerCancelOrder(Long orderId, Long userId, String guestToken) {
        Order order = findOwnedOrder(orderId, userId, guestToken);
        if (order.getStatus() != OrderStatus.PENDING) {
            throw BusinessException.bad("label.order");
        }
        cancelOrder(orderId, "Khách hàng tự hủy đơn");
    }

    /**
     * Lấy danh sách toàn bộ đơn hàng cho Admin/Staff.
     */
    @Override
    @Transactional(readOnly = true)
    public Page<OrderInternalResponse> getAll(OrderFilterRequest filter, Pageable pageable) {
        Page<Order> orders = orderRepository.findAll(OrderSpec.build(filter), pageable);
        Map<Long, Shipment> shipmentsByOrderId = findShipmentsByOrders(orders.getContent());
        return orders.map(order -> orderMapper.toInternalResponse(order, mapItems(order), shipmentsByOrderId.get(order.getId())));
    }

    /**
     * Lấy chi tiết đơn hàng cho Admin/Staff.
     */
    @Override
    @Transactional(readOnly = true)
    public OrderInternalResponse getById(Long id) {
        Order order = findById(id);
        return orderMapper.toInternalResponse(order, mapItems(order), findShipment(order));
    }

    /**
     * Lấy lịch sử chuyển trạng thái đơn hàng cho Admin/Staff.
     */
    @Override
    @Transactional(readOnly = true)
    public List<OrderStatusHistoryResponse> getHistory(Long id) {
        return mapHistory(findById(id));
    }

    /**
     * Cập nhật trạng thái đơn hàng (dành cho Admin/Staff).
     * Điều kiện kiểm tra quyền hạn (Role-based):
     * - Nếu HỦY đơn (CANCELLED) khi đang PENDING: Chỉ ADMIN và SALE được phép (vì chưa bàn giao kho). Các role khác (như WAREHOUSE) bị từ chối.
     * - Nếu XÁC NHẬN đơn (CONFIRMED) từ PENDING: Tương tự, chỉ ADMIN và SALE được phép.
     * - Nếu đánh dấu ĐÃ GIAO (DELIVERED) thủ công: Chỉ ADMIN được phép làm lối thoát hiểm. (Đường chính thống là qua syncShipmentStatus).
     * Sau khi vượt qua kiểm tra quyền, luồng sẽ gọi changeStatus() để đảm bảo State Machine hợp lệ.
     */
    @Override
    public OrderInternalResponse updateStatus(Long id, UpdateOrderStatusRequest request, Long changedByUserId, Collection<String> callerRoles) {
        Order current = findById(id);
        if (request.status() == OrderStatus.CANCELLED) {
            if (current.getStatus() == OrderStatus.PENDING
                    && !callerRoles.contains("ROLE_ADMIN") && !callerRoles.contains("ROLE_SALE")) {
                throw BusinessException.of(ErrorCode.ACCESS_DENIED);
            }
            cancelOrder(id, StringUtils.hasText(request.note()) ? request.note() : "Admin hủy đơn");
        } else {
            if (request.status() == OrderStatus.SHIPPING) {
                throw BusinessException.bad("error.order.shipping_status_readonly");
            }
            if (current.getStatus() == OrderStatus.PENDING && request.status() == OrderStatus.CONFIRMED
                    && !callerRoles.contains("ROLE_ADMIN") && !callerRoles.contains("ROLE_SALE")) {
                throw BusinessException.of(ErrorCode.ACCESS_DENIED);
            }
            if (request.status() == OrderStatus.DELIVERED && !callerRoles.contains("ROLE_ADMIN")) {
                throw BusinessException.of(ErrorCode.ACCESS_DENIED);
            }
            changeStatus(id, request.status(), changedByUserId, request.note());
        }
        Order order = findById(id);
        return orderMapper.toInternalResponse(order, mapItems(order), findShipment(order));
    }

    /**
     * Tạo vận đơn qua đối tác giao hàng (GHN).
     * Luồng xử lý và kiểm tra:
     * 1. Kiểm tra dòng lịch sử Shipment gần nhất: Nếu đã có mã vận đơn và chưa bị hủy, bỏ qua và trả về kết quả luôn.
     * 2. Kiểm tra trạng thái đơn hàng: Phải là CONFIRMED (đã xác nhận) thì mới được tạo vận đơn.
     * 3. Tính toán số tiền thu hộ (COD):
     *    - Nếu thanh toán COD: Tiền thu hộ = Tổng tiền đơn - Phí ship.
     *    - Nếu thanh toán VNPay: Tiền thu hộ = 0.
     * 4. Gọi API GHN (chạy ngoài transaction để tránh treo connection).
     * 5. Mở transaction mới: Lưu thông tin Shipment lấy từ GHN, và đổi trạng thái Order thành SHIPPING.
     */
    @Override
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public OrderInternalResponse createShipment(Long orderId, Long changedByUserId) {
        Order order = findById(orderId);

        Optional<Shipment> existing = shipmentRepository.findFirstByOrder_IdOrderByIdDesc(orderId);
        if (existing.isPresent() && !GHN_CANCEL_STATUS.equals(existing.get().getStatus())) {
            return buildInternalResponse(orderId);
        }

        if (order.getStatus() != OrderStatus.CONFIRMED) {
            throw BusinessException.bad("label.order");
        }

        List<ParcelItem> items = orderItemRepository.findAllByOrder(order).stream()
                .map(ParcelItem::from)
                .toList();

        Long codAmount;
        Integer paymentTypeId;
        if (order.getPaymentMethod() == PaymentMethod.COD) {
            codAmount = order.getTotalAmount().subtract(order.getShippingFee()).longValue();
            paymentTypeId = 2;
        } else {
            codAmount = 0L;
            paymentTypeId = 1;
        }

        CreateShipmentCommand command = new CreateShipmentCommand(
                order.getRecipientName(), order.getRecipientPhone(), order.getShippingStreet(),
                order.getShippingWard(), order.getShippingDistrict(), order.getShippingProvince(),
                order.getCode(), codAmount, paymentTypeId, order.getNote(), items
        );

        ShipmentResult result = shippingService.createShipment(command);

        runInNewTransaction(() -> {
            Shipment shipment = new Shipment();
            shipment.setOrder(entityManager.getReference(Order.class, orderId));
            shipment.setShippingOrderCode(result.shippingOrderCode());
            shipment.setExpectedDeliveryAt(result.expectedDeliveryAt());
            shipmentRepository.save(shipment);

            changeStatus(orderId, OrderStatus.SHIPPING, changedByUserId,
                    "Đã tạo vận đơn GHN, mã: " + result.shippingOrderCode());
        });

        return buildInternalResponse(orderId);
    }

    /**
     * Hủy vận đơn trên hệ thống đối tác giao hàng (GHN).
     * Luồng xử lý và kiểm tra:
     * 1. Kiểm tra trạng thái đơn: Nếu đơn đã DELIVERED hoặc CANCELLED thì từ chối xử lý.
     * 2. Lấy thông tin Shipment gần nhất và gọi API hủy của GHN (chạy ngoài transaction).
     * 3. Mở transaction mới: Đánh dấu trạng thái của Shipment này thành 'cancel'.
     * 4. Hoàn trạng thái Order từ SHIPPING về lại CONFIRMED.
     */
    @Override
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public OrderInternalResponse cancelShipment(Long orderId, Long changedByUserId) {
        Order order = findById(orderId);
        Shipment shipment = shipmentRepository.findFirstByOrder_IdOrderByIdDesc(orderId)
                .orElseThrow(() -> BusinessException.notFound("label.order"));

        if (order.getStatus() == OrderStatus.DELIVERED || order.getStatus() == OrderStatus.CANCELLED) {
            throw BusinessException.bad("label.order");
        }

        shippingService.cancelShipment(shipment.getShippingOrderCode());

        Long shipmentId = shipment.getId();
        String shippingOrderCode = shipment.getShippingOrderCode();
        
        runInNewTransaction(() -> {
            Shipment freshShipment = shipmentRepository.findById(shipmentId)
                    .orElseThrow(() -> BusinessException.notFound("label.order"));
            freshShipment.setStatus(GHN_CANCEL_STATUS);
            shipmentRepository.save(freshShipment);
            changeStatus(orderId, OrderStatus.CONFIRMED, changedByUserId,
                    "Đã hủy vận đơn GHN, mã: " + shippingOrderCode);
        });

        return buildInternalResponse(orderId);
    }

    /**
     * Đồng bộ trạng thái vận đơn mới nhất từ đối tác giao hàng (GHN).
     * Luồng xử lý và kiểm tra:
     * 1. Lấy trạng thái Shipment mới nhất qua API GHN.
     * 2. Mở transaction mới, cập nhật trạng thái và thời gian giao hàng vào bảng Shipment.
     * 3. Xử lý logic theo trạng thái GHN trả về:
     *    - Nếu GHN báo hủy (cancel): Gọi hàm hủy đơn hàng (cancelOrder).
     *    - Nếu GHN báo đã giao (delivered): Đổi PaymentStatus thành PAID (nếu là COD) và đổi OrderStatus thành DELIVERED.
     *    - Nếu GHN báo các trạng thái đang giao khác: Đảm bảo OrderStatus là SHIPPING.
     */
    @Override
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public OrderInternalResponse syncShipmentStatus(Long orderId, Long changedByUserId) {
        Order order = findById(orderId);
        Shipment shipment = shipmentRepository.findFirstByOrder_IdOrderByIdDesc(orderId)
                .orElseThrow(() -> BusinessException.notFound("label.order"));

        if (order.getStatus() == OrderStatus.DELIVERED || order.getStatus() == OrderStatus.CANCELLED) {
            return buildInternalResponse(orderId);
        }

        ShipmentStatusResult result = shippingService.getShipmentStatus(order.getCode());
        Long shipmentId = shipment.getId();
        
        runInNewTransaction(() -> {
            Shipment freshShipment = shipmentRepository.findById(shipmentId)
                    .orElseThrow(() -> BusinessException.notFound("label.order"));
            freshShipment.setStatus(result.status());
            if (result.expectedDeliveryAt() != null) {
                freshShipment.setExpectedDeliveryAt(result.expectedDeliveryAt());
            }
            freshShipment.setDeliveredAt(result.deliveredAt());
            freshShipment.setSyncedAt(LocalDateTime.now());
            shipmentRepository.save(freshShipment);

            Order freshOrder = findById(orderId);

            if (GHN_CANCEL_STATUS.equals(result.status())) {
                cancelOrder(orderId, "GHN báo trạng thái: " + result.status());
            } else if (GHN_DELIVERED_STATUS.equals(result.status())) {
                if (freshOrder.getPaymentStatus() != OrderPaymentStatus.PAID) {
                    freshOrder.setPaymentStatus(OrderPaymentStatus.PAID);
                    orderRepository.save(freshOrder);
                }
                if (freshOrder.getStatus() != OrderStatus.DELIVERED) {
                    changeStatus(orderId, OrderStatus.DELIVERED, changedByUserId, "GHN báo đã giao thành công");
                }
            } else if (freshOrder.getStatus() != OrderStatus.SHIPPING) {
                changeStatus(orderId, OrderStatus.SHIPPING, changedByUserId, "Đồng bộ trạng thái GHN: " + result.status());
            }
        });

        return buildInternalResponse(orderId);
    }

    // Các hàm helper fetch Entity cơ bản
    @Override
    @Transactional(readOnly = true)
    public Order findEntityById(Long id) {
        return findById(id);
    }

    @Override
    public Order findEntityByIdForUpdate(Long id) {
        return orderRepository.findByIdForUpdate(id).orElseThrow(() -> BusinessException.notFound("label.order"));
    }

    @Override
    @Transactional(readOnly = true)
    public Order findOwnedEntityById(Long orderId, Long userId, String guestToken) {
        return findOwnedOrder(orderId, userId, guestToken);
    }

    /**
     * Thực hiện chuyển đổi trạng thái đơn hàng và ghi lại lịch sử.
     * Điều kiện và luồng xử lý:
     * 1. State Machine Check: Trạng thái mới phải nằm trong danh sách hợp lệ (ALLOWED_TRANSITIONS) của trạng thái hiện tại. Nếu vi phạm, ném lỗi.
     * 2. Lưu trạng thái mới và tạo 1 dòng ghi chú trong OrderStatusHistory.
     * 3. Xử lý tích/trừ điểm thành viên (Loyalty):
     *    - Nếu trạng thái mới là DELIVERED: Cộng điểm tích lũy cho Customer.
     *    - Nếu trạng thái mới là CANCELLED (mà trước đó chưa cancel): Thu hồi lại điểm đã cộng.
     */
    @Override
    public void changeStatus(Long orderId, OrderStatus newStatus, Long changedByUserId, String note) {
        Order order = findById(orderId);
        OrderStatus oldStatus = order.getStatus();
        
        if (oldStatus != newStatus && !ALLOWED_TRANSITIONS.getOrDefault(oldStatus, EnumSet.noneOf(OrderStatus.class)).contains(newStatus)) {
            throw BusinessException.bad("label.order");
        }
        order.setStatus(newStatus);
        orderRepository.save(order);

        OrderStatusHistory history = new OrderStatusHistory();
        history.setOrder(order);
        history.setChangedBy(changedByUserId != null ? entityManager.getReference(User.class, changedByUserId) : null);
        history.setOldStatus(oldStatus);
        history.setNewStatus(newStatus);
        history.setNote(note);
        orderStatusHistoryRepository.save(history);

        if (newStatus == OrderStatus.DELIVERED && oldStatus != OrderStatus.DELIVERED && order.getCustomer() != null) {
            BigDecimal earnedAmount = order.getSubtotal().subtract(order.getDiscountAmount());
            customerService.earnFromOrder(order.getCustomer().getId(), orderId, earnedAmount);
        } else if (newStatus == OrderStatus.CANCELLED && oldStatus != OrderStatus.CANCELLED && order.getCustomer() != null) {
            BigDecimal revokedAmount = order.getSubtotal().subtract(order.getDiscountAmount());
            customerService.revokeFromOrder(order.getCustomer().getId(), orderId, revokedAmount);
        }
    }

    /**
     * Đánh dấu đơn hàng đã thanh toán thành công (thường dùng sau khi callback từ VNPay).
     */
    @Override
    public void markAsPaid(Long orderId) {
        Order order = findById(orderId);
        order.setPaymentStatus(OrderPaymentStatus.PAID);
        orderRepository.save(order);
        changeStatus(orderId, OrderStatus.CONFIRMED, null, "Thanh toán VNPay thành công");
    }

    /**
     * Ghi chú vào đơn hàng khi có dấu hiệu thanh toán trễ hoặc lỗi.
     */
    @Override
    public void flagLatePayment(Long orderId, String note) {
        Order order = findById(orderId);
        String existing = order.getAdminNote();
        order.setAdminNote(existing == null || existing.isBlank() ? note : existing + "\n" + note);
        orderRepository.save(order);
    }

    // Các hàm truy vấn phụ trợ cho module khác
    @Override
    @Transactional(readOnly = true)
    public Optional<Order> findDeliveredOrderForProduct(Long userId, Long productId) {
        List<Order> orders = orderItemRepository.findOrdersForProductByStatus(
                userId, productId, OrderStatus.DELIVERED, PageRequest.of(0, 1));
        return orders.isEmpty() ? Optional.empty() : Optional.of(orders.getFirst());
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderItem> findItemsByOrder(Order order) {
        return orderItemRepository.findAllByOrder(order);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderItem findItemById(Long orderItemId) {
        return orderItemRepository.findById(orderItemId)
                .orElseThrow(() -> BusinessException.notFound("label.order.item"));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<LocalDateTime> findDeliveredAt(Long orderId) {
        return orderStatusHistoryRepository
                .findTopByOrder_IdAndNewStatusOrderByCreatedAtDesc(orderId, OrderStatus.DELIVERED)
                .map(OrderStatusHistory::getCreatedAt);
    }

    /**
     * Xử lý logic nghiệp vụ nền khi hủy đơn hàng.
     * Điều kiện kiểm tra:
     * - Nếu đơn đã CANCELLED hoặc DELIVERED từ trước rồi thì return ngay (no-op).
     * Luồng xử lý:
     * 1. Vòng lặp qua các OrderItem, cộng lại số lượng vào tồn kho (ProductVariant).
     * 2. Gọi InventoryTransactionService để ghi log hoàn kho.
     * 3. Giải phóng số lần sử dụng của mã giảm giá (Coupon).
     * 4. Gọi changeStatus để đổi thành CANCELLED và ghi lịch sử.
     * 5. Bắn sự kiện OrderCancelledEvent để các module khác (như Notification) xử lý tiếp.
     */
    @Override
    public void cancelOrder(Long orderId, String reason) {
        Order order = orderRepository.findByIdForUpdate(orderId)
                .orElseThrow(() -> BusinessException.notFound("label.order"));
        if (order.getStatus() == OrderStatus.CANCELLED || order.getStatus() == OrderStatus.DELIVERED) {
            return;
        }

        for (OrderItem item : orderItemRepository.findAllByOrder(order)) {
            Long variantId = item.getVariant().getId();
            StockChangeResult stockChange = productVariantService.increaseStock(variantId, item.getQuantity());
            inventoryTransactionService.record(variantId, null, InventoryTransactionType.IN, item.getQuantity(),
                    stockChange.oldStock(), stockChange.newStock(), InventoryReferenceType.ORDER, order.getId(),
                    "Hoàn kho do hủy đơn #" + order.getCode());
        }

        couponService.releaseUsage(order);
        changeStatus(orderId, OrderStatus.CANCELLED, null, reason);

        eventPublisher.publishEvent(new OrderCancelledEvent(order.getId(), order.getCode(), reason));
    }

    // ---- Private helpers ----

    private record ResolvedItem(ProductVariant variant, int quantity, BigDecimal unitPrice, BigDecimal originalPrice) {}

    private record OrderPricing(
            BigDecimal subtotal,
            BigDecimal shippingFee,
            BigDecimal discountAmount,
            BigDecimal tierDiscountAmount,
            BigDecimal totalAmount,
            CouponApplyResult couponResult
    ) {}

    /**
     * Xác định email của người đặt hàng.
     * Cập nhật mới: Email của Guest hiện tại là tùy chọn (Optional).
     * Bẫy Bot (Honeypot) đã được chuyển lên xử lý ở khối validation trước đó.
     */
    private String resolveCustomerEmail(Long userId, CreateOrderRequest request, boolean isGuest) {
        if (isGuest) {
            return request.guestEmail();
        }
        return customerService.getOrCreateByUserId(userId).getUser().getEmail();
    }

    /**
     * Lấy các sản phẩm đang được chọn trong giỏ hàng.
     * Luồng xử lý và kiểm tra:
     * 1. Lấy toàn bộ giỏ hàng của User/Guest.
     * 2. Lọc ra các món có cờ selected = true. 
     *    - Nếu danh sách lọc ra rỗng (không chọn món nào) -> ném lỗi 'label.cart'.
     * 3. Lấy thông tin giá cả (giá hiện tại, giá gốc) của từng biến thể (Variant) từ DB.
     *    - Đảm bảo biến thể phải đang active (thông qua findAllActiveByIds).
     * 4. Đóng gói vào ResolvedItem để các bước sau tính toán.
     */
    private List<ResolvedItem> resolveCartItems(Long userId, String guestToken) {
        CartResponse cart = cartService.getCart(userId, guestToken);
        List<CartItemResponse> selectedItems = cart.items().stream()
                .filter(CartItemResponse::selected)
                .toList();
        if (selectedItems.isEmpty()) {
            throw BusinessException.bad("label.cart");
        }

        List<Long> variantIds = selectedItems.stream().map(CartItemResponse::variantId).toList();
        Map<Long, ProductVariant> variantsById = productVariantService.findAllActiveByIds(variantIds).stream()
                .collect(Collectors.toMap(ProductVariant::getId, Function.identity()));

        return selectedItems.stream()
                .map(item -> {
                    ProductVariant variant = variantsById.get(item.variantId());
                    BigDecimal unitPrice = variant.getPrice();
                    BigDecimal originalPrice = variant.getOriginalPrice();
                    return new ResolvedItem(variant, item.quantity(), unitPrice, originalPrice);
                })
                .toList();
    }

    /**
     * Tính toán toàn bộ chi phí của đơn hàng (tiền hàng, phí ship, mã giảm giá).
     * Luồng xử lý:
     * 1. Tính tổng tiền hàng (subtotal) = Tổng (đơn giá x số lượng).
     * 2. Kiểm tra Coupon:
     *    - Nếu có truyền couponCode -> gọi couponService.validate để kiểm tra (hết hạn, đủ điều kiện, số lượng).
     *    - Ghi nhận số tiền được giảm (discountAmount).
     * 3. Tính phí giao hàng (shippingFee) thông qua API GHN (hoặc rule local) dựa trên địa chỉ nhận và khối lượng hàng.
     * 4. Tính tổng tiền thanh toán (totalAmount) = Tiền hàng + Phí ship - Tiền giảm.
     */
    private OrderPricing calculateOrderPricing(CreateOrderRequest request, List<ResolvedItem> resolvedItems, String email, Customer customer) {
        BigDecimal subtotal = resolvedItems.stream()
                .map(ri -> ri.unitPrice().multiply(BigDecimal.valueOf(ri.quantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        CouponApplyResult couponResult = null;
        BigDecimal discountAmount = BigDecimal.ZERO;
        if (StringUtils.hasText(request.couponCode())) {
            couponResult = couponService.validate(request.couponCode(), subtotal, email, request.recipientPhone());
            discountAmount = couponResult.discountAmount();
        }

        // 3. Tính tier discount dựa trên subtotal (hoặc subtotal - discountAmount, ở đây tính trên subtotal)
        BigDecimal tierDiscountAmount = BigDecimal.ZERO;
        if (customer != null && customer.getMembershipTier() != MembershipTier.BRONZE) {
            StoreSettingResponse setting = settingService.getStoreSetting();
            Integer rate = switch (customer.getMembershipTier()) {
                case SILVER -> setting.silverDiscountRate();
                case GOLD -> setting.goldDiscountRate();
                case PLATINUM -> setting.platinumDiscountRate();
                default -> 0;
            };
            if (rate != null && rate > 0) {
                tierDiscountAmount = subtotal.multiply(BigDecimal.valueOf(rate)).divide(BigDecimal.valueOf(100));
            }
        }

        List<ParcelItem> parcelItems = resolvedItems.stream()
                .map(ri -> ParcelItem.from(ri.variant(), ri.quantity()))
                .toList();
                
        CalculateShippingFeeCommand shippingFeeCommand = new CalculateShippingFeeCommand(
                request.recipientName(), request.recipientPhone(), request.shippingStreet(),
                request.shippingWard(), request.shippingDistrict(), request.shippingProvince(),
                subtotal, parcelItems
        );
        BigDecimal shippingFee = shippingService.calculateShippingFee(shippingFeeCommand).fee();
        
        BigDecimal totalAmount = subtotal.add(shippingFee).subtract(discountAmount).subtract(tierDiscountAmount);
        if (totalAmount.compareTo(BigDecimal.ZERO) < 0) {
            totalAmount = BigDecimal.ZERO;
        }

        return new OrderPricing(subtotal, shippingFee, discountAmount, tierDiscountAmount, totalAmount, couponResult);
    }

    /**
     * Build và lưu entity Order xuống cơ sở dữ liệu.
     * Điều kiện kiểm tra:
     * - Bọc trong khối try/catch bắt DataIntegrityViolationException.
     * - Vì trường idempotency_key trong DB có unique constraint, nếu 2 luồng cùng pass qua check ban đầu 
     *   và cùng lúc insert, DB sẽ ném lỗi. Bắt lỗi này và ném ra BusinessException "đơn hàng đã tồn tại".
     */
    private Order saveOrderEntity(Customer customer, String guestToken, String idempotencyKey, 
                                  CreateOrderRequest request, OrderPricing pricing, boolean isGuest) {
        Order order = new Order();
        order.setCustomer(customer);
        order.setGuestToken(isGuest ? guestToken : null);
        order.setCode(generateOrderCode());
        order.setTrackingToken(UUID.randomUUID().toString());
        order.setIdempotencyKey(idempotencyKey);
        order.setStatus(OrderStatus.PENDING);
        order.setPaymentStatus(OrderPaymentStatus.UNPAID);
        order.setPaymentMethod(request.paymentMethod());
        order.setRecipientName(request.recipientName());
        order.setRecipientPhone(request.recipientPhone());
        order.setShippingStreet(request.shippingStreet());
        order.setShippingWard(request.shippingWard());
        order.setShippingDistrict(request.shippingDistrict());
        order.setShippingProvince(request.shippingProvince());
        order.setSubtotal(pricing.subtotal());
        order.setShippingFee(pricing.shippingFee());
        order.setDiscountAmount(pricing.discountAmount());
        order.setTierDiscountAmount(pricing.tierDiscountAmount());
        order.setTotalAmount(pricing.totalAmount());
        order.setCouponCode(pricing.couponResult() != null ? pricing.couponResult().code() : null);
        order.setNote(request.note());
        order.setExpiredAt(LocalDateTime.now().plusMinutes(orderProperties.getExpireMinutes()));
        
        try {
            return orderRepository.saveAndFlush(order);
        } catch (DataIntegrityViolationException e) {
            throw BusinessException.alreadyExists("label.order");
        }
    }

    /**
     * Xử lý lưu chi tiết đơn hàng (OrderItem) và trừ tồn kho.
     * Luồng xử lý:
     * 1. Vòng lặp qua từng ResolvedItem đã tính toán từ giỏ hàng.
     * 2. Gọi productVariantService.decreaseStock để trừ kho cứng. (Sẽ ném lỗi nếu không đủ tồn kho).
     * 3. Lưu OrderItem với thông tin snapshot (giá, tên, size) để không bị ảnh hưởng nếu sản phẩm đổi giá sau này.
     * 4. Ghi log lịch sử biến động kho (InventoryTransaction) với loại OUT (xuất).
     */
    private List<OrderItem> processOrderItems(Order order, List<ResolvedItem> resolvedItems, Long userId) {
        List<OrderItem> savedItems = new ArrayList<>();
        for (ResolvedItem ri : resolvedItems) {
            StockChangeResult stockChange = productVariantService.decreaseStock(ri.variant().getId(), ri.quantity());

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setVariant(ri.variant());
            orderItem.setProductCode(ri.variant().getProduct().getCode());
            orderItem.setProductName(ri.variant().getProduct().getName());
            orderItem.setVariantSku(ri.variant().getSku());
            orderItem.setVariantSize(ri.variant().getSize());
            orderItem.setVariantColor(ri.variant().getColorway());
            orderItem.setQuantity(ri.quantity());
            orderItem.setOriginalPrice(ri.originalPrice());
            orderItem.setUnitPrice(ri.unitPrice());
            orderItem.setSubtotal(ri.unitPrice().multiply(BigDecimal.valueOf(ri.quantity())));
            
            savedItems.add(orderItemRepository.save(orderItem));

            inventoryTransactionService.record(ri.variant().getId(), userId, InventoryTransactionType.OUT, ri.quantity(),
                    stockChange.oldStock(), stockChange.newStock(), InventoryReferenceType.ORDER, order.getId(),
                    "Trừ kho khi tạo đơn #" + order.getCode());
        }
        return savedItems;
    }

    /**
     * Xử lý các tác vụ dọn dẹp và ghi nhận sau khi tạo đơn hàng thành công.
     * Luồng xử lý:
     * 1. Tạo bản ghi lịch sử trạng thái đầu tiên (PENDING).
     * 2. Nếu có áp dụng mã giảm giá -> ghi nhận số lần sử dụng coupon của user này.
     * 3. Xóa các món đã đặt khỏi giỏ hàng (giữ lại các món không chọn).
     * 4. Phát sự kiện (OrderCreatedEvent) để các module khác (như Notification gửi email xác nhận) bắt đầu chạy.
     */
    private void processPostOrderCreation(Order order, OrderPricing pricing, String email, 
                                          CreateOrderRequest request, boolean isGuest, 
                                          String guestToken, Long userId) {
        OrderStatusHistory initialHistory = new OrderStatusHistory();
        initialHistory.setOrder(order);
        initialHistory.setOldStatus(null);
        initialHistory.setNewStatus(OrderStatus.PENDING);
        initialHistory.setNote("Tạo đơn hàng");
        orderStatusHistoryRepository.save(initialHistory);

        if (pricing.couponResult() != null) {
            couponService.recordUsage(pricing.couponResult().couponId(), order, email, request.recipientPhone());
        }

        cartService.clearSelectedItems(userId, guestToken);

        eventPublisher.publishEvent(new OrderCreatedEvent(
                order.getId(), order.getCode(), email, request.recipientName(), 
                pricing.totalAmount(), order.getTrackingToken()));
    }

    /**
     * Sinh mã đơn hàng ngẫu nhiên, định dạng: ORD + Timestamp + 3 số ngẫu nhiên.
     */
    private String generateOrderCode() {
        return "ORD" + System.currentTimeMillis() + ThreadLocalRandom.current().nextInt(100, 999);
    }

    /**
     * Hàm phụ trợ chuyển đổi danh sách OrderItem entity sang DTO response.
     */
    private List<OrderItemResponse> mapItems(Order order) {
        return orderItemRepository.findAllByOrder(order).stream().map(orderMapper::toItemResponse).toList();
    }

    /**
     * Hàm phụ trợ lấy và chuyển đổi lịch sử trạng thái đơn hàng (sắp xếp tăng dần theo thời gian tạo).
     */
    private List<OrderStatusHistoryResponse> mapHistory(Order order) {
        return orderStatusHistoryRepository.findByOrderOrderByCreatedAtAsc(order).stream()
                .map(orderMapper::toHistoryResponse).toList();
    }

    /**
     * Tìm thông tin vận đơn gần nhất của một đơn hàng.
     * Dựa vào ID giảm dần để lấy dòng Shipment được tạo sau cùng.
     */
    private Shipment findShipment(Order order) {
        return shipmentRepository.findFirstByOrder_IdOrderByIdDesc(order.getId()).orElse(null);
    }

    /**
     * Tìm thông tin vận đơn gần nhất cho một danh sách đơn hàng.
     * Xử lý gom nhóm: Nếu 1 Order có nhiều Shipment, luôn ưu tiên giữ lại Shipment có ID lớn nhất (mới nhất).
     */
    private Map<Long, Shipment> findShipmentsByOrders(List<Order> orders) {
        List<Long> orderIds = orders.stream().map(Order::getId).toList();
        return shipmentRepository.findAllByOrder_IdIn(orderIds).stream()
                .collect(Collectors.toMap(s -> s.getOrder().getId(), Function.identity(),
                        (a, b) -> a.getId() > b.getId() ? a : b));
    }

    /**
     * Đóng gói OrderInternalResponse trong một transaction chỉ đọc (ReadOnly).
     * Luồng xử lý:
     * - Các method gọi API GHN (createShipment, cancelShipment, vv.) được đánh dấu NOT_SUPPORTED (không có transaction DB).
     * - Để Mapper lấy được các trường lazy-load (như customer user email), bắt buộc phải mở thủ công 1 transaction bằng TransactionTemplate.
     */
    private OrderInternalResponse buildInternalResponse(Long orderId) {
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        transactionTemplate.setReadOnly(true);
        return transactionTemplate.execute(status -> {
            Order order = findById(orderId);
            return orderMapper.toInternalResponse(order, mapItems(order), findShipment(order));
        });
    }

    /**
     * Chạy khối lệnh thao tác DB trong một Transaction mới (RequiresNew mô phỏng).
     * Sử dụng TransactionTemplate để ép buộc Spring mở transaction thật sự, hữu ích khi ghi dữ liệu 
     * sau một thao tác mạng kéo dài (chạy ở context NOT_SUPPORTED).
     */
    private void runInNewTransaction(Runnable action) {
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        transactionTemplate.executeWithoutResult(status -> action.run());
    }

    /**
     * Tìm đơn hàng dựa theo quyền sở hữu (User ID hoặc Guest Token).
     * Điều kiện kiểm tra:
     * - Phải cung cấp ít nhất userId hoặc guestToken.
     * - Yêu cầu DB tìm đúng đơn khớp với thông tin định danh tương ứng. Ném lỗi nếu không tìm thấy hoặc sai chủ.
     */
    private Order findOwnedOrder(Long orderId, Long userId, String guestToken) {
        requireIdentity(userId, guestToken);
        Optional<Order> orderOpt = userId != null
                ? orderRepository.findByIdAndCustomer_User_Id(orderId, userId)
                : orderRepository.findByIdAndGuestToken(orderId, guestToken);
        return orderOpt.orElseThrow(() -> BusinessException.notFound("label.order"));
    }

    /**
     * Đảm bảo request có thông tin định danh (không được null cả userId lẫn guestToken).
     * Tránh lỗi bảo mật khi ai đó có thể quét đơn hàng vô chủ.
     */
    private void requireIdentity(Long userId, String guestToken) {
        if (userId == null && !StringUtils.hasText(guestToken)) {
            throw BusinessException.notFound("label.order");
        }
    }

    /**
     * Tìm đơn hàng theo ID. Bọc sẵn ngoại lệ BusinessException nếu không tìm thấy.
     */
    private Order findById(Long id) {
        return orderRepository.findById(id).orElseThrow(() -> BusinessException.notFound("label.order"));
    }
}
