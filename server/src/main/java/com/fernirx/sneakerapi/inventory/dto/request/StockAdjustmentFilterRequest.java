package com.fernirx.sneakerapi.inventory.dto.request;

import com.fernirx.sneakerapi.inventory.enums.StockAdjustmentStatus;
import com.fernirx.sneakerapi.inventory.enums.StockAdjustmentType;

import java.time.LocalDateTime;

public record StockAdjustmentFilterRequest(
        String search,
        StockAdjustmentType type,
        StockAdjustmentStatus status,
        LocalDateTime fromDate,
        LocalDateTime toDate
) {}
