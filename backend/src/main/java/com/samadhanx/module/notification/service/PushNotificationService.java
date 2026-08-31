package com.samadhanx.module.notification.service;

import com.samadhanx.module.notification.dto.NotificationResponse;
import com.samadhanx.module.notification.dto.RegisterDeviceTokenRequest;
import com.samadhanx.module.notification.entity.enums.NotificationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface PushNotificationService {

    void registerDeviceToken(UUID userId, RegisterDeviceTokenRequest request);

    void unregisterDeviceToken(UUID userId, String token);

    void sendNotificationToUser(
            UUID userId,
            String title,
            String body,
            NotificationType type,
            String referenceId,
            String referenceType
    );

    Page<NotificationResponse> getUserNotifications(UUID userId, Pageable pageable);

    void markAsRead(UUID notificationId, UUID userId);

    void markAllAsRead(UUID userId);

    long getUnreadCount(UUID userId);

    java.util.List<com.samadhanx.module.notification.dto.EcosystemActivityFeedItem> getEcosystemActivityFeed(int limit);
}
