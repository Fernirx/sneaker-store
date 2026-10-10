package com.fernirx.sneakerapi.notification.dto.response;

import com.fernirx.sneakerapi.notification.enums.NotificationType;

import java.time.LocalDateTime;

public record NotificationResponse(
        Long id,
        NotificationType type,
        String title,
        String message,
        String imagePublicId,
        String link,
        boolean read,
        LocalDateTime createdAt
) {}
