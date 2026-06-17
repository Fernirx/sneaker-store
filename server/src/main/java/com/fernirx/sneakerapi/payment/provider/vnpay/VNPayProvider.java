package com.fernirx.sneakerapi.payment.provider.vnpay;

import com.fernirx.sneakerapi.payment.dto.request.PaymentRequest;
import com.fernirx.sneakerapi.payment.provider.PaymentProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class VNPayProvider implements PaymentProvider {
    private final VNPayClient client;
    private final VNPaySignature signature;

    @Override
    public String buildPaymentUrl(PaymentRequest request, String ipAddress) {
        Map<String, String> params = client.buildBaseParams(request, ipAddress);
        String hash = signature.sign(params);
        return client.buildUrl(params, hash);
    }

    @Override
    public boolean verifySignature(Map<String, String> params) {
        return signature.verify(params);
    }
}