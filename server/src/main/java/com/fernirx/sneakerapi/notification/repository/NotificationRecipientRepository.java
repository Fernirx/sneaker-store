package com.fernirx.sneakerapi.notification.repository;

import com.fernirx.sneakerapi.notification.entity.NotificationRecipient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface NotificationRecipientRepository extends JpaRepository<NotificationRecipient, Long> {

    @Query(value = "SELECT r FROM NotificationRecipient r JOIN FETCH r.notification n WHERE r.user.id = :userId AND n.active = true ORDER BY r.createdAt DESC",
            countQuery = "SELECT COUNT(r) FROM NotificationRecipient r WHERE r.user.id = :userId AND r.notification.active = true")
    Page<NotificationRecipient> findByUserId(@Param("userId") Long userId, Pageable pageable);

    Optional<NotificationRecipient> findByNotification_IdAndUser_Id(Long notificationId, Long userId);

    @Query("SELECT COUNT(r) FROM NotificationRecipient r WHERE r.user.id = :userId AND r.readAt IS NULL AND r.notification.active = true")
    long countUnreadByUserId(@Param("userId") Long userId);
}
