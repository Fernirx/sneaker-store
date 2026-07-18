package com.fernirx.sneakerapi.returns.service;

import com.fernirx.sneakerapi.returns.dto.request.CreateReturnRequest;
import com.fernirx.sneakerapi.returns.dto.request.ProcessReturnRequest;
import com.fernirx.sneakerapi.returns.dto.request.RejectReturnRequest;
import com.fernirx.sneakerapi.returns.dto.request.ReturnFilterRequest;
import com.fernirx.sneakerapi.returns.dto.request.UpdateTrackingRequest;
import com.fernirx.sneakerapi.returns.dto.response.EligibleOrderItemResponse;
import com.fernirx.sneakerapi.returns.dto.response.ReturnRequestInternalResponse;
import com.fernirx.sneakerapi.returns.dto.response.ReturnRequestResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ReturnRequestService {

    // Customer
    List<EligibleOrderItemResponse> getEligibleItems(Long orderId, Long userId);
    ReturnRequestResponse create(Long userId, CreateReturnRequest request);
    Page<ReturnRequestResponse> getMyReturns(Long userId, Long orderId, Pageable pageable);
    ReturnRequestResponse getMyReturnDetail(Long id, Long userId);
    ReturnRequestResponse updateTracking(Long id, Long userId, UpdateTrackingRequest request);

    // Admin
    Page<ReturnRequestInternalResponse> getAll(ReturnFilterRequest filter, Pageable pageable);
    ReturnRequestInternalResponse getById(Long id);
    ReturnRequestInternalResponse approve(Long id, Long approvedByUserId);
    ReturnRequestInternalResponse reject(Long id, RejectReturnRequest request);
    ReturnRequestInternalResponse markReceived(Long id);
    ReturnRequestInternalResponse process(Long id, Long processedByUserId, ProcessReturnRequest request);
}
