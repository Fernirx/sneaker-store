package com.fernirx.sneakerapi.notification.service;

import java.math.BigDecimal;

public interface MailService {
    void sendVerifyEmailOtp(String to, String username, String otpCode, int expiryMinutes);
    void sendForgotPasswordOtp(String to, String username, String otpCode, int expiryMinutes);
    void sendOrderConfirmation(String to, String recipientName, String orderCode, BigDecimal totalAmount, String trackingToken, String trackingUrl);
}
