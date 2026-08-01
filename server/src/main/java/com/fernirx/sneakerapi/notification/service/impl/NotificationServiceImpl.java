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

    @Override
    @Transactional(readOnly = true)
    public Page<NotificationResponse> getMyNotifications(Long userId, Pageable pageable) {
        return notificationRecipientRepository.findByUserId(userId, pageable).map(notificationMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public NotificationResponse getMyNotificationDetail(Long notificationId, Long userId) {
        NotificationRecipient recipient = notificationRecipientRepository
                .findByNotification_IdAndUser_Id(notificationId, userId)
                .orElseThrow(() -> BusinessException.notFound("label.notification"));
        return notificationMapper.toResponse(recipient);
    }

    @Override
    @Transactional(readOnly = true)
    public long getUnreadCount(Long userId) {
        return notificationRecipientRepository.countUnreadByUserId(userId);
    }

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

    @Override
    @Transactional(readOnly = true)
    public SseEmitter subscribe(Long userId) {
        return sseEmitterRegistry.register(userId);
    }

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
        // Sanitize tập trung tại điểm ghi DB duy nhất - bảo vệ mọi nguồn gọi create() hiện tại (event
        // listener, message ghép chuỗi tự động) lẫn tương lai, không phụ thuộc từng caller tự sanitize.
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

        // Không sanitize ở đây nữa - create() giờ tự sanitize title+message cho mọi caller (xem create()).
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

    @Override
    @Transactional(readOnly = true)
    public Page<NotificationInternalResponse> getMarketingHistory(Pageable pageable) {
        return notificationRepository
                .findByTargetTypeInOrderByCreatedAtDesc(List.of(NotificationTargetType.ALL, NotificationTargetType.USER), pageable)
                .map(notificationMapper::toInternalResponse);
    }

    @Override
    public NotificationInternalResponse setActive(Long id, boolean active) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> BusinessException.notFound("label.notification"));
        notification.setActive(active);
        return notificationMapper.toInternalResponse(notificationRepository.save(notification));
    }

    private List<Long> resolveRecipientUserIds(CreateNotificationCommand command) {
        return switch (command.targetType()) {
            case USER -> command.targetUserIds() != null ? command.targetUserIds() : List.of();
            case ROLE -> userRoleRepository.findUserIdsByRole(command.targetRole());
            case ALL -> userRoleRepository.findUserIdsByRole(Role.ROLE_CUSTOMER);
        };
    }
}
