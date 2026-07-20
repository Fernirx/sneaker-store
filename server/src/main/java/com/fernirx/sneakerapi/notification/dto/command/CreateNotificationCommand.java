package com.fernirx.sneakerapi.notification.dto.command;

import com.fernirx.sneakerapi.common.enums.Role;
import com.fernirx.sneakerapi.notification.enums.NotificationTargetType;
import com.fernirx.sneakerapi.notification.enums.NotificationType;

import java.util.List;

/**
 * Command nội bộ (không expose qua API) dùng chung cho cả 2 nguồn tạo thông báo:
 * NotificationEventListener (vận hành, targetType=ROLE, message thuần text) và
 * NotificationServiceImpl.createMarketing (marketing, targetType=ALL|USER, message HTML đã sanitize).
 */
public record CreateNotificationCommand(
        NotificationType type,
        NotificationTargetType targetType,
        Role targetRole,
        List<Long> targetUserIds,
        String title,
        String message,
        String imagePublicId,
        String link
) {
    public static CreateNotificationCommand toRole(NotificationType type, Role role, String title, String message, String link) {
        return new CreateNotificationCommand(type, NotificationTargetType.ROLE, role, null, title, message, null, link);
    }

    public static CreateNotificationCommand toAllCustomers(NotificationType type, String title, String message, String link) {
        return new CreateNotificationCommand(type, NotificationTargetType.ALL, null, null, title, message, null, link);
    }

    public static CreateNotificationCommand toUser(NotificationType type, Long userId, String title, String message, String link) {
        return new CreateNotificationCommand(type, NotificationTargetType.USER, null, List.of(userId), title, message, null, link);
    }
}
