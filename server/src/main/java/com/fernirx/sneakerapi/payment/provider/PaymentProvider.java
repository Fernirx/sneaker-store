package com.fernirx.sneakerapi.payment.provider;

import com.fernirx.sneakerapi.payment.dto.request.BuildPaymentUrlRequest;

import java.util.Map;

public interface PaymentProvider {
    String buildPaymentUrl(BuildPaymentUrlRequest request, String ipAddress);
    boolean verifySignature(Map<String, String> params);
}