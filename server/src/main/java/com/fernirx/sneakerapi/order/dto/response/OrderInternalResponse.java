package com.fernirx.sneakerapi.order.dto.response;

import com.fernirx.sneakerapi.order.enums.OrderPaymentStatus;
import com.fernirx.sneakerapi.order.enums.OrderStatus;
import com.fernirx.sneakerapi.order.enums.PaymentMethod;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderInternalResponse(
        Long id,
        String code,
        OrderStatus status,
        OrderPaymentStatus paymentStatus,
        PaymentMethod paymentMethod,
        String customerEmail,
        String guestToken,
        String recipientName,
        String recipientPhone,
        String shippingStreet,
        String shippingWard,
        String shippingDistrict,
        String shippingProvince,
        BigDecimal subtotal,
        BigDecimal shippingFee,
        BigDecimal discountAmount,
        BigDecimal totalAmount,
        String couponCode,
        String note,
        String adminNote,
        Long assignedToId,
        LocalDateTime expiredAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        List<OrderItemResponse> items
) {}
