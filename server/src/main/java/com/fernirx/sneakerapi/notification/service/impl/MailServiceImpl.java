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

    /**
     * Gửi email mã OTP xác thực email khi đăng ký tài khoản.
     * Quá trình gửi được chạy ngầm (Async) để không làm block luồng chính.
     */
    @Override
    @Async
    public void sendVerifyEmailOtp(String to, String username, String otpCode, int expiryMinutes) {
        Context context = buildOtpContext(username, otpCode, expiryMinutes);
        String html = templateEngine.process("mail/verify-email", context);
        mailProvider.send(to, MessageUtil.getMessage("mail.verify.email.subject"), html);
    }

    /**
     * Gửi email mã OTP cấp lại mật khẩu.
     */
    @Override
    @Async
    public void sendForgotPasswordOtp(String to, String username, String otpCode, int expiryMinutes) {
        Context context = buildOtpContext(username, otpCode, expiryMinutes);
        String html = templateEngine.process("mail/forgot-password", context);
        mailProvider.send(to, MessageUtil.getMessage("mail.forgot.password.subject"), html);
    }

    /**
     * Gửi email xác nhận đơn hàng thành công kèm theo link tra cứu đơn hàng.
     */
    @Override
    @Async
    public void sendOrderConfirmation(String to, String recipientName, String orderCode, BigDecimal totalAmount, String trackingToken, String trackingUrl) {
        Context context = new Context();
        context.setVariable("recipientName", recipientName);
        context.setVariable("orderCode", orderCode);
        context.setVariable("totalAmountFormatted", NumberFormat.getInstance(new Locale("vi", "VN")).format(totalAmount) + "đ");
        context.setVariable("trackingToken", trackingToken);
        context.setVariable("trackingUrl", trackingUrl);
        String html = templateEngine.process("mail/order-confirmation", context);
        mailProvider.send(to, MessageUtil.getMessage("mail.order_confirmation.subject", orderCode), html);
    }

    /**
     * Helper tạo Context chung cho các loại email OTP.
     */
    private Context buildOtpContext(String username, String otpCode, int expiryMinutes) {
        Context context = new Context();
        context.setVariable("username", username);
        context.setVariable("otpCode", otpCode);
        context.setVariable("expiryMinutes", expiryMinutes);
        return context;
    }
}
