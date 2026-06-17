package com.fernirx.sneakerapi.payment.service;

import com.fernirx.sneakerapi.payment.dto.request.PaymentRequest;

import java.util.Map;

public interface PaymentService {
    String create(PaymentRequest paymentRequest, String ipAddress);
    Map<String, String> handleIpn(Map<String, String> params);
}
