package com.fernirx.sneakerapi.supplier.service;

import com.fernirx.sneakerapi.supplier.dto.request.CancelPurchaseRequest;
import com.fernirx.sneakerapi.supplier.dto.request.CreatePurchaseRequest;
import com.fernirx.sneakerapi.supplier.dto.request.PurchaseFilterRequest;
import com.fernirx.sneakerapi.supplier.dto.request.ReceivePurchaseRequest;
import com.fernirx.sneakerapi.supplier.dto.request.UpdatePurchaseRequest;
import com.fernirx.sneakerapi.supplier.dto.response.PurchaseResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PurchaseService {
    Page<PurchaseResponse> getAll(PurchaseFilterRequest filter, Pageable pageable);
    PurchaseResponse getById(Long id);
    PurchaseResponse create(CreatePurchaseRequest request, Long createdByUserId);
    PurchaseResponse update(Long id, UpdatePurchaseRequest request);
    PurchaseResponse confirm(Long id);
    PurchaseResponse receive(Long id, ReceivePurchaseRequest request, Long receivedByUserId);
    PurchaseResponse cancel(Long id, CancelPurchaseRequest request);
    PurchaseResponse markAsPaid(Long id);
}
