package com.fernirx.sneakerapi.payment.provider;

import com.fernirx.sneakerapi.payment.dto.request.PaymentRequest;

import java.util.Map;

public interface PaymentProvider {
    String buildPaymentUrl(PaymentRequest request, String ipAddress);
    boolean verifySignature(Map<String, String> params);
}