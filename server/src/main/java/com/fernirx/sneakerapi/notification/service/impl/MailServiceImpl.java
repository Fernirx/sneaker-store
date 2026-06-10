package com.fernirx.sneakerapi.notification.service.impl;

import com.fernirx.sneakerapi.common.utils.MessageUtil;
import com.fernirx.sneakerapi.notification.provider.MailProvider;
import com.fernirx.sneakerapi.notification.service.MailService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

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

    private Context buildOtpContext(String username, String otpCode, int expiryMinutes) {
        Context context = new Context();
        context.setVariable("username", username);
        context.setVariable("otpCode", otpCode);
        context.setVariable("expiryMinutes", expiryMinutes);
        return context;
    }
}
