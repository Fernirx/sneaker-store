package com.fernirx.sneakerapi.notification.service.impl;

import com.fernirx.sneakerapi.common.enums.Role;
import com.fernirx.sneakerapi.common.exception.BusinessException;
import com.fernirx.sneakerapi.notification.dto.command.CreateNotificationCommand;
import com.fernirx.sneakerapi.notification.dto.request.CreateNotificationRequest;
import com.fernirx.sneakerapi.notification.dto.response.NotificationInternalResponse;
import com.fernirx.sneakerapi.notification.dto.response.NotificationResponse;
import com.fernirx.sneakerapi.notification.entity.Notification;
import com.fernirx.sneakerapi.notification.entity.NotificationRecipient;
import com.fernirx.sneakerapi.notification.enums.NotificationTargetType;
import com.fernirx.sneakerapi.notification.mapper.NotificationMapper;
import com.fernirx.sneakerapi.notification.repository.NotificationRecipientRepository;
import com.fernirx.sneakerapi.notification.repository.NotificationRepository;
import com.fernirx.sneakerapi.notification.service.NotificationService;
import com.fernirx.sneakerapi.notification.sse.SseEmitterRegistry;
import com.fernirx.sneakerapi.customer.repository.CustomerRepository;
import com.fernirx.sneakerapi.user.entity.User;
import com.fernirx.sneakerapi.user.repository.UserRoleRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.owasp.html.PolicyFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationRecipientRepository notificationRecipientRepository;
    private final NotificationMapper notificationMapper;
    private final UserRoleRepository userRoleRepository;
    private final CustomerRepository customerRepository;
    private final SseEmitterRegistry sseEmitterRegistry;
    private final PolicyFactory richTextHtmlPolicy;

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * Lấy danh sách thông báo của người dùng hiện tại (có phân trang).
     */
    @Override
    @Transactional(readOnly = true)
    public Page<NotificationResponse> getMyNotifications(Long userId, Pageable pageable) {
        return notificationRecipientRepository.findByUserId(userId, pageable).map(notificationMapper::toResponse);
    }

    /**
     * Lấy chi tiết một thông báo của người dùng.
     * Đảm bảo tính bảo mật (IDOR): Chỉ lấy được thông báo thuộc về chính userId này.
     */
    @Override
    @Transactional(readOnly = true)
    public NotificationResponse getMyNotificationDetail(Long notificationId, Long userId) {
        NotificationRecipient recipient = notificationRecipientRepository
                .findByNotification_IdAndUser_Id(notificationId, userId)
                .orElseThrow(() -> BusinessException.notFound("label.notification"));
        return notificationMapper.toResponse(recipient);
    }

    /**
     * Đếm số lượng thông báo chưa đọc của người dùng.
     * Dùng để hiển thị badge số lượng đỏ góc màn hình.
     */
    @Override
    @Transactional(readOnly = true)
    public long getUnreadCount(Long userId) {
        return notificationRecipientRepository.countUnreadByUserId(userId);
    }

    /**
     * Đánh dấu một thông báo là đã đọc.
     * Cập nhật thời gian readAt nếu chưa được đánh dấu.
     */
    @Override
    public void markAsRead(Long notificationId, Long userId) {
        NotificationRecipient recipient = notificationRecipientRepository
                .findByNotification_IdAndUser_Id(notificationId, userId)
                .orElseThrow(() -> BusinessException.notFound("label.notification"));
        if (recipient.getReadAt() == null) {
            recipient.setReadAt(LocalDateTime.now());
            notificationRecipientRepository.save(recipient);
        }
    }

    /**
     * Đăng ký kết nối SSE (Server-Sent Events) để nhận thông báo realtime (Push Notification).
     */
    @Override
    @Transactional(readOnly = true)
    public SseEmitter subscribe(Long userId) {
        return sseEmitterRegistry.register(userId);
    }

    /**
     * Tạo thông báo mới và Push qua SSE tới người dùng.
     * Hỗ trợ chống XSS bằng cách sanitize nội dung HTML trước khi lưu.
     */
    @Override
    public Notification create(CreateNotificationCommand command) {
        List<Long> recipientUserIds = resolveRecipientUserIds(command);
        if (recipientUserIds.isEmpty()) {
            return null;
        }

        Notification notification = new Notification();
        notification.setType(command.type());
        notification.setTargetType(command.targetType());
        notification.setTargetRole(command.targetRole());
        if (command.targetType() == NotificationTargetType.USER && recipientUserIds.size() == 1) {
            notification.setTargetUser(entityManager.getReference(User.class, recipientUserIds.get(0)));
        }
        
        notification.setTitle(richTextHtmlPolicy.sanitize(command.title()));
        notification.setMessage(richTextHtmlPolicy.sanitize(command.message()));
        notification.setImagePublicId(command.imagePublicId());
        notification.setLink(command.link());
        notification.setActive(true);
        notification = notificationRepository.save(notification);

        Notification savedNotification = notification;
        List<NotificationRecipient> recipients = recipientUserIds.stream().map(userId -> {
            NotificationRecipient recipient = new NotificationRecipient();
            recipient.setNotification(savedNotification);
            recipient.setUser(entityManager.getReference(User.class, userId));
            return recipient;
        }).toList();
        notificationRecipientRepository.saveAll(recipients);

        NotificationResponse payload = notificationMapper.toPushPayload(savedNotification);
        sseEmitterRegistry.sendToUsers(recipientUserIds, payload);
        return savedNotification;
    }

    /**
     * CMS - Tạo thông báo thủ công (Marketing/Thông báo chung).
     * Chỉ được phép gửi cho tất cả (ALL) hoặc danh sách Khách hàng cụ thể (USER).
     */
    @Override
    public NotificationInternalResponse createMarketing(CreateNotificationRequest request) {
        if (request.targetType() == NotificationTargetType.ROLE) {
            throw BusinessException.bad("label.notification");
        }
        if (request.targetType() == NotificationTargetType.USER && CollectionUtils.isEmpty(request.targetCustomerIds())) {
            throw BusinessException.bad("label.notification");
        }

        List<Long> targetUserIds = request.targetType() == NotificationTargetType.USER
                ? customerRepository.findAllById(request.targetCustomerIds()).stream().map(c -> c.getUser().getId()).toList()
                : null;

        CreateNotificationCommand command = new CreateNotificationCommand(
                request.type(),
                request.targetType(),
                null,
                targetUserIds,
                request.title(),
                request.message(),
                request.imagePublicId(),
                request.link()
        );
        Notification saved = create(command);
        if (saved == null) {
            throw BusinessException.bad("label.notification");
        }
        return notificationMapper.toInternalResponse(saved);
    }

    /**
     * CMS - Lấy danh sách lịch sử gửi thông báo Marketing.
     */
    @Override
    @Transactional(readOnly = true)
    public Page<NotificationInternalResponse> getMarketingHistory(Pageable pageable) {
        return notificationRepository
                .findByTargetTypeInOrderByCreatedAtDesc(List.of(NotificationTargetType.ALL, NotificationTargetType.USER), pageable)
                .map(notificationMapper::toInternalResponse);
    }

    /**
     * CMS - Bật/Tắt (Thu hồi) thông báo. 
     * Khi active = false thì thông báo sẽ bị ẩn khỏi list của khách.
     */
    @Override
    public NotificationInternalResponse setActive(Long id, boolean active) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> BusinessException.notFound("label.notification"));
        notification.setActive(active);
        return notificationMapper.toInternalResponse(notificationRepository.save(notification));
    }

    /**
     * Helper xác định danh sách User ID nhận thông báo dựa vào TargetType.
     */
    private List<Long> resolveRecipientUserIds(CreateNotificationCommand command) {
        return switch (command.targetType()) {
            case USER -> command.targetUserIds() != null ? command.targetUserIds() : List.of();
            case ROLE -> userRoleRepository.findUserIdsByRole(command.targetRole());
            case ALL -> userRoleRepository.findUserIdsByRole(Role.ROLE_CUSTOMER);
        };
    }
}
