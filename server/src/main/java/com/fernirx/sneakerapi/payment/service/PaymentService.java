package com.fernirx.sneakerapi.payment.service;

import com.fernirx.sneakerapi.payment.dto.request.PaymentFilterRequest;
import com.fernirx.sneakerapi.payment.dto.response.PaymentInternalResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Map;

public interface PaymentService {
    String create(Long orderId, String ipAddress);
    Map<String, String> handleIpn(Map<String, String> params);
    Page<PaymentInternalResponse> getAll(PaymentFilterRequest filter, Pageable pageable);
    PaymentInternalResponse getById(Long id);
}
