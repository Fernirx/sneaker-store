package com.fernirx.sneakerapi.returns.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fernirx.sneakerapi.returns.enums.ReturnResolutionType;
import com.fernirx.sneakerapi.returns.enums.ReturnStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ReturnRequestResponse(
        Long id,
        String code,
        Long orderId,
        String orderCode,
        ReturnResolutionType resolutionType,
        ReturnStatus status,
        String reason,
        String rejectReason,
        String trackingCode,
        BigDecimal refundAmount,
        LocalDateTime refundedAt,
        String exchangeShippingOrderCode,
        LocalDateTime exchangeExpectedDeliveryAt,
        LocalDateTime createdAt,
        List<ReturnRequestItemResponse> items,
        List<String> imagePublicIds
) {}
