package com.fernirx.sneakerapi.inventory.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fernirx.sneakerapi.inventory.enums.StockAdjustmentStatus;
import com.fernirx.sneakerapi.inventory.enums.StockAdjustmentType;

import java.time.LocalDateTime;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record StockAdjustmentResponse(
        Long id,
        String code,
        StockAdjustmentType type,
        StockAdjustmentStatus status,
        String reason,
        Long createdById,
        String createdByEmail,
        Long approvedById,
        String approvedByEmail,
        LocalDateTime approvedAt,
        LocalDateTime confirmedAt,
        LocalDateTime createdAt,
        List<StockAdjustmentItemResponse> items
) {}
