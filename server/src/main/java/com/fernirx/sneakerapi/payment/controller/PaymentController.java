package com.fernirx.sneakerapi.payment.controller;

import com.fernirx.sneakerapi.common.response.SuccessResponse;
import com.fernirx.sneakerapi.payment.dto.request.CreatePaymentRequest;
import com.fernirx.sneakerapi.payment.service.PaymentService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/payment")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {
    private final PaymentService paymentService;

    @PostMapping("/create")
    public ResponseEntity<SuccessResponse<String>> create(
            @Valid @RequestBody CreatePaymentRequest request,
            HttpServletRequest httpRequest) {
        String ipAddress = resolveClientIp(httpRequest);
        String paymentUrl = paymentService.create(request.orderId(), ipAddress);
        return ResponseEntity.ok(SuccessResponse.of(null, paymentUrl));
    }

    @GetMapping("/vnpay-ipn")
    public ResponseEntity<Map<String, String>> handleIpn(
            @RequestParam Map<String, String> params) {
        Map<String, String> result = paymentService.handleIpn(params);
        return ResponseEntity.ok(result);
    }

    private String resolveClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isBlank() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip.contains(",") ? ip.split(",")[0].trim() : ip;
    }
}
