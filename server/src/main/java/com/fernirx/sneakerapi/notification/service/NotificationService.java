package com.fernirx.sneakerapi.notification.service;

import com.fernirx.sneakerapi.notification.dto.command.CreateNotificationCommand;
import com.fernirx.sneakerapi.notification.dto.request.CreateNotificationRequest;
import com.fernirx.sneakerapi.notification.dto.response.NotificationInternalResponse;
import com.fernirx.sneakerapi.notification.dto.response.NotificationResponse;
import com.fernirx.sneakerapi.notification.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public interface NotificationService {
    Page<NotificationResponse> getMyNotifications(Long userId, Pageable pageable);

    NotificationResponse getMyNotificationDetail(Long notificationId, Long userId);

    long getUnreadCount(Long userId);

    void markAsRead(Long notificationId, Long userId);

    SseEmitter subscribe(Long userId);

    /** Dùng nội bộ bởi NotificationEventListener - không sanitize (message do hệ thống tự sinh, không phải input người dùng).
     * Trả về null nếu không resolve được recipient nào (không tạo Notification "mồ côi"). */
    Notification create(CreateNotificationCommand command);

    /** Dùng bởi admin composer - sanitize HTML trước khi lưu. */
    NotificationInternalResponse createMarketing(CreateNotificationRequest request);

    Page<NotificationInternalResponse> getMarketingHistory(Pageable pageable);

    NotificationInternalResponse setActive(Long id, boolean active);
}
