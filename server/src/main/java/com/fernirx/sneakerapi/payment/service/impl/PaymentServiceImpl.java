package com.fernirx.sneakerapi.payment.service.impl;

import com.fernirx.sneakerapi.payment.dto.request.PaymentRequest;
import com.fernirx.sneakerapi.payment.provider.PaymentProvider;
import com.fernirx.sneakerapi.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@Transactional
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {
    private final PaymentProvider paymentProvider;

    @Override
    public String create(PaymentRequest paymentRequest, String ipAddress) {
        return paymentProvider.buildPaymentUrl(paymentRequest, ipAddress);
    }

    @Override
    public Map<String, String> handleIpn(Map<String, String> params) {
        return ipnResponse("00", "Confirm Success");
    }

    private Map<String, String> ipnResponse(String code, String message) {
        return Map.of("RspCode", code, "Message", message);
    }
}
