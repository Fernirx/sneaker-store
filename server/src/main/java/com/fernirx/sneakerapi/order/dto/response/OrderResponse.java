package com.fernirx.sneakerapi.order.dto.response;

import com.fernirx.sneakerapi.order.enums.OrderPaymentStatus;
import com.fernirx.sneakerapi.order.enums.OrderStatus;
import com.fernirx.sneakerapi.order.enums.PaymentMethod;
import com.fernirx.sneakerapi.shipping.dto.response.ShipmentResponse;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderResponse(
        Long id,
        String code,
        String trackingToken,
        OrderStatus status,
        OrderPaymentStatus paymentStatus,
        PaymentMethod paymentMethod,
        String recipientName,
        String recipientPhone,
        String shippingStreet,
        String shippingWard,
        String shippingDistrict,
        String shippingProvince,
        BigDecimal subtotal,
        BigDecimal shippingFee,
        BigDecimal discountAmount,
        BigDecimal tierDiscountAmount,
        BigDecimal totalAmount,
        String couponCode,
        String note,
        LocalDateTime expiredAt,
        LocalDateTime createdAt,
        List<OrderItemResponse> items,
        ShipmentResponse shipment
) {}
