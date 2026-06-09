package com.fernirx.sneakerapi.notification.service;

public interface MailService {
    void sendVerifyEmailOtp(String to, String username, String otpCode, int expiryMinutes);
}
