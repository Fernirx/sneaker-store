package com.fernirx.sneakerapi.common.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import java.text.MessageFormat;

@Component
public class MessageUtil {
    private static MessageSource messageSource;

    @Autowired
    public MessageUtil(MessageSource messageSource) {
        MessageUtil.messageSource = messageSource;
    }

    public static String getMessage(String key) {
        try {
            return messageSource.getMessage(key, null, LocaleContextHolder.getLocale());
        } catch (Exception e) {
            return key;
        }
    }

    public static String getMessage(String key, Object... args) {
        try {
            String template = messageSource.getMessage(key, null, LocaleContextHolder.getLocale());
            return MessageFormat.format(template, args);
        } catch (Exception e) {
            return key;
        }
    }
}