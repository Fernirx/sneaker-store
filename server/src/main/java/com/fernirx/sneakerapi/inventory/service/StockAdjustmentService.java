package com.fernirx.sneakerapi.inventory.service;

import com.fernirx.sneakerapi.inventory.dto.request.CancelStockAdjustmentRequest;
import com.fernirx.sneakerapi.inventory.dto.request.CreateStockAdjustmentRequest;
import com.fernirx.sneakerapi.inventory.dto.request.StockAdjustmentFilterRequest;
import com.fernirx.sneakerapi.inventory.dto.request.UpdateStockAdjustmentRequest;
import com.fernirx.sneakerapi.inventory.dto.response.StockAdjustmentResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface StockAdjustmentService {
    Page<StockAdjustmentResponse> getAll(StockAdjustmentFilterRequest filter, Pageable pageable);
    StockAdjustmentResponse getById(Long id);
    StockAdjustmentResponse create(CreateStockAdjustmentRequest request, Long createdByUserId);
    StockAdjustmentResponse update(Long id, UpdateStockAdjustmentRequest request);
    StockAdjustmentResponse approve(Long id, Long approvedByUserId);
    StockAdjustmentResponse confirm(Long id, Long confirmedByUserId);
    StockAdjustmentResponse cancel(Long id, CancelStockAdjustmentRequest request);
}
