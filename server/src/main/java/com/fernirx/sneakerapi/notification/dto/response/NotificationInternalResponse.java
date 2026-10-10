package com.fernirx.sneakerapi.notification.dto.response;

import com.fernirx.sneakerapi.common.enums.Role;
import com.fernirx.sneakerapi.notification.enums.NotificationTargetType;
import com.fernirx.sneakerapi.notification.enums.NotificationType;

import java.time.LocalDateTime;

public record NotificationInternalResponse(
        Long id,
        NotificationType type,
        String title,
        String message,
        String imagePublicId,
        String link,
        NotificationTargetType targetType,
        Role targetRole,
        Boolean active,
        LocalDateTime createdAt
) {}
