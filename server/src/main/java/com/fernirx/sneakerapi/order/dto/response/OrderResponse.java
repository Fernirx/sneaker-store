package com.fernirx.sneakerapi.order.dto.response;

import com.fernirx.sneakerapi.order.enums.OrderPaymentStatus;
import com.fernirx.sneakerapi.order.enums.OrderStatus;
import com.fernirx.sneakerapi.order.enums.PaymentMethod;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderResponse(
        Long id,
        String code,
        OrderStatus status,
        OrderPaymentStatus paymentStatus,
        PaymentMethod paymentMethod,
        String recipientName,
        String recipientPhone,
        String shippingStreet,
        String shippingWard,
        String shippingWardCode,
        String shippingProvince,
        String shippingProvinceCode,
        BigDecimal subtotal,
        BigDecimal shippingFee,
        BigDecimal discountAmount,
        BigDecimal totalAmount,
        String couponCode,
        String note,
        LocalDateTime expiredAt,
        LocalDateTime createdAt,
        List<OrderItemResponse> items
) {}
