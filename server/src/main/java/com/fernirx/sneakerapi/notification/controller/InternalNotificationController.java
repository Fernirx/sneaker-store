package com.fernirx.sneakerapi.notification.controller;

import com.fernirx.sneakerapi.common.response.PageResponse;
import com.fernirx.sneakerapi.common.response.SuccessResponse;
import com.fernirx.sneakerapi.common.utils.MessageUtil;
import com.fernirx.sneakerapi.notification.dto.request.CreateNotificationRequest;
import com.fernirx.sneakerapi.notification.dto.response.NotificationInternalResponse;
import com.fernirx.sneakerapi.notification.dto.response.NotificationResponse;
import com.fernirx.sneakerapi.notification.service.NotificationService;
import com.fernirx.sneakerapi.security.model.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/internal/notifications")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
@Tag(name = "Internal Notification API", description = "Thông báo vận hành (nhân viên) + soạn thông báo marketing (admin)")
public class InternalNotificationController {
    private final NotificationService notificationService;

    @GetMapping
    @Operation(summary = "Danh sách thông báo vận hành của tôi")
    public ResponseEntity<PageResponse<NotificationResponse>> getMyNotifications(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(PageResponse.of(notificationService.getMyNotifications(userDetails.getId(), pageable)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Chi tiết thông báo")
    public ResponseEntity<SuccessResponse<NotificationResponse>> getDetail(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id) {
        return ResponseEntity.ok(SuccessResponse.of(notificationService.getMyNotificationDetail(id, userDetails.getId())));
    }

    @GetMapping("/unread-count")
    @Operation(summary = "Số thông báo chưa đọc")
    public ResponseEntity<SuccessResponse<Long>> getUnreadCount(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(SuccessResponse.of(notificationService.getUnreadCount(userDetails.getId())));
    }

    @PatchMapping("/{id}/read")
    @Operation(summary = "Đánh dấu đã đọc")
    public ResponseEntity<SuccessResponse<Void>> markAsRead(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id) {
        notificationService.markAsRead(id, userDetails.getId());
        return ResponseEntity.ok(SuccessResponse.of(
                MessageUtil.getMessage("success.resource.updated", MessageUtil.getMessage("label.notification"))
        ));
    }

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "Kênh realtime (SSE)")
    public SseEmitter stream(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return notificationService.subscribe(userDetails.getId());
    }

    @PostMapping("/marketing")
    @PreAuthorize("hasAnyRole('ADMIN', 'MARKETING')")
    @Operation(summary = "Tạo thông báo marketing cho khách hàng")
    public ResponseEntity<SuccessResponse<NotificationInternalResponse>> createMarketing(
            @Valid @RequestBody CreateNotificationRequest request) {
        NotificationInternalResponse response = notificationService.createMarketing(request);
        return ResponseEntity.ok(SuccessResponse.of(
                MessageUtil.getMessage("success.resource.created", MessageUtil.getMessage("label.notification")),
                response
        ));
    }

    @GetMapping("/marketing")
    @PreAuthorize("hasAnyRole('ADMIN', 'MARKETING')")
    @Operation(summary = "Lịch sử thông báo marketing đã gửi")
    public ResponseEntity<PageResponse<NotificationInternalResponse>> getMarketingHistory(
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(PageResponse.of(notificationService.getMarketingHistory(pageable)));
    }

    @PatchMapping("/marketing/{id}/active")
    @PreAuthorize("hasAnyRole('ADMIN', 'MARKETING')")
    @Operation(summary = "Ẩn/hiện thông báo marketing")
    public ResponseEntity<SuccessResponse<NotificationInternalResponse>> setActive(
            @PathVariable Long id,
            @RequestParam boolean active) {
        return ResponseEntity.ok(SuccessResponse.of(notificationService.setActive(id, active)));
    }
}
