package com.fernirx.sneakerapi.notification.service.impl;

import com.fernirx.sneakerapi.common.utils.MessageUtil;
import com.fernirx.sneakerapi.notification.provider.MailProvider;
import com.fernirx.sneakerapi.notification.service.MailService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class MailServiceImpl implements MailService {
    private final MailProvider mailProvider;
    private final SpringTemplateEngine templateEngine;

    @Override
    @Async
    public void sendVerifyEmailOtp(String to, String username, String otpCode, int expiryMinutes) {
        Context context = buildOtpContext(username, otpCode, expiryMinutes);
        String html = templateEngine.process("mail/verify-email", context);
        mailProvider.send(to, MessageUtil.getMessage("mail.verify.email.subject"), html);
    }

    @Override
    @Async
    public void sendForgotPasswordOtp(String to, String username, String otpCode, int expiryMinutes) {
        Context context = buildOtpContext(username, otpCode, expiryMinutes);
        String html = templateEngine.process("mail/forgot-password", context);
        mailProvider.send(to, MessageUtil.getMessage("mail.forgot.password.subject"), html);
    }

    @Override
    @Async
    public void sendOrderVerificationOtp(String to, String username, String otpCode, int expiryMinutes) {
        Context context = buildOtpContext(username, otpCode, expiryMinutes);
        String html = templateEngine.process("mail/guest-order-otp", context);
        mailProvider.send(to, MessageUtil.getMessage("mail.guest_order.subject"), html);
    }

    @Override
    @Async
    public void sendOrderConfirmation(String to, String recipientName, String orderCode, BigDecimal totalAmount, String orderUrl) {
        Context context = new Context();
        context.setVariable("recipientName", recipientName);
        context.setVariable("orderCode", orderCode);
        context.setVariable("totalAmountFormatted", NumberFormat.getInstance(new Locale("vi", "VN")).format(totalAmount) + "đ");
        context.setVariable("orderUrl", orderUrl);
        String html = templateEngine.process("mail/order-confirmation", context);
        mailProvider.send(to, MessageUtil.getMessage("mail.order_confirmation.subject", orderCode), html);
    }

    private Context buildOtpContext(String username, String otpCode, int expiryMinutes) {
        Context context = new Context();
        context.setVariable("username", username);
        context.setVariable("otpCode", otpCode);
        context.setVariable("expiryMinutes", expiryMinutes);
        return context;
    }
}
