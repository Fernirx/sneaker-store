package com.fernirx.sneakerapi.notification.dto.request;

import com.fernirx.sneakerapi.notification.enums.NotificationTargetType;
import com.fernirx.sneakerapi.notification.enums.NotificationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record CreateNotificationRequest(
        @NotNull NotificationType type,
        @NotBlank @Size(max = 255) String title,
        @NotBlank String message,
        @NotNull NotificationTargetType targetType,
        /** ID của Customer (không phải User) - admin chọn "khách hàng cụ thể" theo domain khách hàng, chỉ dùng khi targetType=USER. */
        List<Long> targetCustomerIds,
        @Size(max = 255) String imagePublicId,
        @Size(max = 500) String link
) {}
