package com.fernirx.sneakerapi.returns.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fernirx.sneakerapi.returns.enums.ReturnResolutionType;
import com.fernirx.sneakerapi.returns.enums.ReturnStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ReturnRequestInternalResponse(
        Long id,
        String code,
        Long orderId,
        String orderCode,
        Long customerId,
        String customerEmail,
        ReturnResolutionType resolutionType,
        ReturnStatus status,
        String reason,
        String rejectReason,
        String trackingCode,
        BigDecimal refundAmount,
        LocalDateTime refundedAt,
        String exchangeShippingOrderCode,
        LocalDateTime exchangeExpectedDeliveryAt,
        Long approvedById,
        String approvedByEmail,
        LocalDateTime approvedAt,
        LocalDateTime receivedAt,
        LocalDateTime completedAt,
        String adminNote,
        LocalDateTime createdAt,
        List<ReturnRequestItemResponse> items,
        List<String> imagePublicIds
) {}
