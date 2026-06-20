package com.fernirx.sneakerapi.order.service.impl;

import com.fernirx.sneakerapi.auth.service.OtpService;
import com.fernirx.sneakerapi.cart.dto.response.CartItemResponse;
import com.fernirx.sneakerapi.cart.dto.response.CartResponse;
import com.fernirx.sneakerapi.cart.service.CartService;
import com.fernirx.sneakerapi.common.exception.BusinessException;
import com.fernirx.sneakerapi.coupon.dto.response.CouponApplyResult;
import com.fernirx.sneakerapi.coupon.service.CouponService;
import com.fernirx.sneakerapi.customer.entity.Customer;
import com.fernirx.sneakerapi.customer.service.CustomerService;
import com.fernirx.sneakerapi.inventory.enums.InventoryReferenceType;
import com.fernirx.sneakerapi.inventory.enums.InventoryTransactionType;
import com.fernirx.sneakerapi.inventory.service.InventoryTransactionService;
import com.fernirx.sneakerapi.order.config.OrderProperties;
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
import com.fernirx.sneakerapi.order.mapper.OrderMapper;
import com.fernirx.sneakerapi.order.repository.OrderItemRepository;
import com.fernirx.sneakerapi.order.repository.OrderRepository;
import com.fernirx.sneakerapi.order.repository.OrderSpec;
import com.fernirx.sneakerapi.order.repository.OrderStatusHistoryRepository;
import com.fernirx.sneakerapi.order.service.OrderService;
import com.fernirx.sneakerapi.product.dto.response.StockChangeResult;
import com.fernirx.sneakerapi.product.entity.ProductVariant;
import com.fernirx.sneakerapi.product.service.ProductVariantService;
import com.fernirx.sneakerapi.user.entity.User;
import com.fernirx.sneakerapi.user.enums.OtpPurpose;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

@Service
@Transactional
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

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
    private final OtpService otpService;

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public void sendGuestOtp(String email) {
        otpService.sendOtp(email, null, OtpPurpose.GUEST_ORDER);
    }

    @Override
    public OrderResponse createOrder(Long userId, String guestToken, CreateOrderRequest request) {
        boolean isGuest = userId == null;
        Customer customer = null;
        String email;

        if (isGuest) {
            if (!StringUtils.hasText(request.guestEmail()) || !StringUtils.hasText(request.otpCode())) {
                throw BusinessException.bad("label.otp");
            }
            otpService.verifyOtp(request.guestEmail(), request.otpCode(), OtpPurpose.GUEST_ORDER);
            email = request.guestEmail();
        } else {
            customer = customerService.getOrCreateByUserId(userId);
            email = customer.getUser().getEmail();
        }

        CartResponse cart = cartService.getCart(userId, guestToken);
        List<CartItemResponse> selectedItems = cart.items().stream()
                .filter(CartItemResponse::selected)
                .toList();
        if (selectedItems.isEmpty()) {
            throw BusinessException.bad("label.cart");
        }

        List<ResolvedItem> resolvedItems = selectedItems.stream()
                .map(item -> {
                    ProductVariant variant = productVariantService.findActiveById(item.variantId());
                    BigDecimal unitPrice = resolveUnitPrice(variant);
                    BigDecimal originalPrice = resolveOriginalPrice(variant, unitPrice);
                    return new ResolvedItem(variant, item.quantity(), unitPrice, originalPrice);
                })
                .toList();

        BigDecimal subtotal = resolvedItems.stream()
                .map(ri -> ri.unitPrice().multiply(BigDecimal.valueOf(ri.quantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        CouponApplyResult couponResult = null;
        BigDecimal discountAmount = BigDecimal.ZERO;
        if (StringUtils.hasText(request.couponCode())) {
            couponResult = couponService.validate(request.couponCode(), subtotal, email, request.recipientPhone());
            discountAmount = couponResult.discountAmount();
        }

        BigDecimal shippingFee = orderProperties.getShippingFee();
        BigDecimal totalAmount = subtotal.add(shippingFee).subtract(discountAmount);

        Order order = new Order();
        order.setCustomer(customer);
        order.setGuestToken(isGuest ? guestToken : null);
        order.setCode(generateOrderCode());
        order.setStatus(OrderStatus.PENDING);
        order.setPaymentStatus(OrderPaymentStatus.UNPAID);
        order.setPaymentMethod(request.paymentMethod());
        order.setRecipientName(request.recipientName());
        order.setRecipientPhone(request.recipientPhone());
        order.setShippingStreet(request.shippingStreet());
        order.setShippingWard(request.shippingWard());
        order.setShippingDistrict(request.shippingDistrict());
        order.setShippingProvince(request.shippingProvince());
        order.setSubtotal(subtotal);
        order.setShippingFee(shippingFee);
        order.setDiscountAmount(discountAmount);
        order.setTotalAmount(totalAmount);
        order.setCouponCode(couponResult != null ? couponResult.code() : null);
        order.setNote(request.note());
        order.setExpiredAt(LocalDateTime.now().plusMinutes(orderProperties.getExpireMinutes()));
        order = orderRepository.save(order);

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

        OrderStatusHistory initialHistory = new OrderStatusHistory();
        initialHistory.setOrder(order);
        initialHistory.setOldStatus(null);
        initialHistory.setNewStatus(OrderStatus.PENDING);
        initialHistory.setNote("Tạo đơn hàng");
        orderStatusHistoryRepository.save(initialHistory);

        if (couponResult != null) {
            couponService.recordUsage(couponResult.couponId(), order, email, request.recipientPhone());
        }

        cartService.clearSelectedItems(userId, guestToken);

        List<OrderItemResponse> itemResponses = savedItems.stream().map(orderMapper::toItemResponse).toList();
        return orderMapper.toResponse(order, itemResponses);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderResponse> getMyOrders(Long userId, String guestToken, Pageable pageable) {
        requireIdentity(userId, guestToken);
        Page<Order> orders = userId != null
                ? orderRepository.findByCustomer_User_Id(userId, pageable)
                : orderRepository.findByGuestToken(guestToken, pageable);
        return orders.map(order -> orderMapper.toResponse(order, mapItems(order)));
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getMyOrderDetail(Long orderId, Long userId, String guestToken) {
        Order order = findOwnedOrder(orderId, userId, guestToken);
        return orderMapper.toResponse(order, mapItems(order));
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderStatusHistoryResponse> getMyOrderHistory(Long orderId, Long userId, String guestToken) {
        Order order = findOwnedOrder(orderId, userId, guestToken);
        return mapHistory(order);
    }

    @Override
    public void customerCancelOrder(Long orderId, Long userId, String guestToken) {
        Order order = findOwnedOrder(orderId, userId, guestToken);
        if (order.getStatus() != OrderStatus.PENDING) {
            throw BusinessException.bad("label.order");
        }
        cancelOrder(orderId, "Khách hàng tự hủy đơn");
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderInternalResponse> getAll(OrderFilterRequest filter, Pageable pageable) {
        return orderRepository.findAll(OrderSpec.build(filter), pageable)
                .map(order -> orderMapper.toInternalResponse(order, mapItems(order)));
    }

    @Override
    @Transactional(readOnly = true)
    public OrderInternalResponse getById(Long id) {
        Order order = findById(id);
        return orderMapper.toInternalResponse(order, mapItems(order));
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderStatusHistoryResponse> getHistory(Long id) {
        return mapHistory(findById(id));
    }

    @Override
    public OrderInternalResponse updateStatus(Long id, UpdateOrderStatusRequest request, Long changedByUserId) {
        if (request.status() == OrderStatus.CANCELLED) {
            cancelOrder(id, StringUtils.hasText(request.note()) ? request.note() : "Admin hủy đơn");
        } else {
            changeStatus(id, request.status(), changedByUserId, request.note());
        }
        Order order = findById(id);
        return orderMapper.toInternalResponse(order, mapItems(order));
    }

    @Override
    @Transactional(readOnly = true)
    public Order findEntityById(Long id) {
        return findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Order findOwnedEntityById(Long orderId, Long userId, String guestToken) {
        return findOwnedOrder(orderId, userId, guestToken);
    }

    @Override
    public void changeStatus(Long orderId, OrderStatus newStatus, Long changedByUserId, String note) {
        Order order = findById(orderId);
        OrderStatus oldStatus = order.getStatus();
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
            customerService.earnFromOrder(order.getCustomer().getId(), earnedAmount);
        }
    }

    @Override
    public void markAsPaid(Long orderId) {
        Order order = findById(orderId);
        order.setPaymentStatus(OrderPaymentStatus.PAID);
        orderRepository.save(order);
        changeStatus(orderId, OrderStatus.CONFIRMED, null, "Thanh toán VNPay thành công");
    }

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

        couponService.releaseUsage(order.getId());
        changeStatus(orderId, OrderStatus.CANCELLED, null, reason);
    }

    // ---- Private helpers ----

    private record ResolvedItem(ProductVariant variant, int quantity, BigDecimal unitPrice, BigDecimal originalPrice) {}

    private BigDecimal resolveUnitPrice(ProductVariant variant) {
        BigDecimal variantPrice = variant.getPrice();
        return variantPrice != null ? variantPrice : variant.getProduct().getBasePrice();
    }

    private BigDecimal resolveOriginalPrice(ProductVariant variant, BigDecimal unitPrice) {
        BigDecimal originalPrice = variant.getProduct().getOriginalPrice();
        if (originalPrice == null) return null;
        return originalPrice.compareTo(unitPrice) > 0 ? originalPrice : null;
    }

    private String generateOrderCode() {
        return "ORD" + System.currentTimeMillis() + ThreadLocalRandom.current().nextInt(100, 999);
    }

    private List<OrderItemResponse> mapItems(Order order) {
        return orderItemRepository.findAllByOrder(order).stream().map(orderMapper::toItemResponse).toList();
    }

    private List<OrderStatusHistoryResponse> mapHistory(Order order) {
        return orderStatusHistoryRepository.findByOrderOrderByCreatedAtAsc(order).stream()
                .map(orderMapper::toHistoryResponse).toList();
    }

    private Order findOwnedOrder(Long orderId, Long userId, String guestToken) {
        requireIdentity(userId, guestToken);
        Optional<Order> orderOpt = userId != null
                ? orderRepository.findByIdAndCustomer_User_Id(orderId, userId)
                : orderRepository.findByIdAndGuestToken(orderId, guestToken);
        return orderOpt.orElseThrow(() -> BusinessException.notFound("label.order"));
    }

    private void requireIdentity(Long userId, String guestToken) {
        if (userId == null && !StringUtils.hasText(guestToken)) {
            throw BusinessException.notFound("label.order");
        }
    }

    private Order findById(Long id) {
        return orderRepository.findById(id).orElseThrow(() -> BusinessException.notFound("label.order"));
    }
}
