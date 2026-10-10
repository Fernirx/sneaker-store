package com.fernirx.sneakerapi.notification.repository;

import com.fernirx.sneakerapi.notification.entity.Notification;
import com.fernirx.sneakerapi.notification.enums.NotificationTargetType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    Page<Notification> findByTargetTypeInOrderByCreatedAtDesc(List<NotificationTargetType> targetTypes, Pageable pageable);
}
