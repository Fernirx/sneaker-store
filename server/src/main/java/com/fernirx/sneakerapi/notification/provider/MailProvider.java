package com.fernirx.sneakerapi.notification.provider;

public interface MailProvider {
    void send(String to, String subject, String htmlContent);
}
