package com.fernirx.sneakerapi.common.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;

import org.springframework.stereotype.Component;

import java.text.MessageFormat;

@Component
public class MessageUtil {
    private static MessageSource messageSource;

    private static final java.util.Locale VI_LOCALE = java.util.Locale.forLanguageTag("vi");

    @Autowired
    public MessageUtil(MessageSource messageSource) {
        MessageUtil.messageSource = messageSource;
    }

    public static String getMessage(String key) {
        try {
            return messageSource.getMessage(key, null, VI_LOCALE);
        } catch (Exception e) {
            return key;
        }
    }

    public static String getMessage(String key, Object... args) {
        try {
            String template = messageSource.getMessage(key, null, VI_LOCALE);
            return MessageFormat.format(template, args);
        } catch (Exception e) {
            return key;
        }
    }
}