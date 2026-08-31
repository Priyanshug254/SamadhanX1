package com.samadhanx.module.notification.service;

import com.google.firebase.FirebaseApp;
import com.google.firebase.messaging.AndroidConfig;
import com.google.firebase.messaging.AndroidNotification;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import com.samadhanx.common.exception.ResourceNotFoundException;
import com.samadhanx.module.notification.dto.NotificationResponse;
import com.samadhanx.module.notification.dto.RegisterDeviceTokenRequest;
import com.samadhanx.module.notification.entity.DeviceToken;
import com.samadhanx.module.notification.entity.NotificationRecord;
import com.samadhanx.module.notification.entity.enums.NotificationType;
import com.samadhanx.module.notification.repository.DeviceTokenRepository;
import com.samadhanx.module.notification.repository.NotificationRecordRepository;
import com.samadhanx.module.user.entity.User;
import com.samadhanx.module.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PushNotificationServiceImpl implements PushNotificationService {

    private static final Logger log = LoggerFactory.getLogger(PushNotificationServiceImpl.class);

    private final DeviceTokenRepository deviceTokenRepository;
    private final NotificationRecordRepository notificationRecordRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public void registerDeviceToken(UUID userId, RegisterDeviceTokenRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        Optional<DeviceToken> existing = deviceTokenRepository.findByToken(request.getToken().trim());
        if (existing.isPresent()) {
            DeviceToken token = existing.get();
            token.setUser(user);
            token.setDeviceType(request.getDeviceType());
            token.setLastActiveAt(Instant.now());
            deviceTokenRepository.save(token);
            log.info("Updated FCM device token for user: {}", user.getEmail());
        } else {
            DeviceToken token = DeviceToken.builder()
                    .user(user)
                    .token(request.getToken().trim())
                    .deviceType(request.getDeviceType())
                    .lastActiveAt(Instant.now())
                    .build();
            deviceTokenRepository.save(token);
            log.info("Registered new FCM device token for user: {}", user.getEmail());
        }
    }

    @Override
    @Transactional
    public void unregisterDeviceToken(UUID userId, String token) {
        if (token != null && !token.isBlank()) {
            deviceTokenRepository.deleteByUserIdAndToken(userId, token.trim());
            log.info("Unregistered FCM device token for user ID: {}", userId);
        }
    }

    @Override
    @Transactional
    public void sendNotificationToUser(
            UUID userId,
            String title,
            String body,
            NotificationType type,
            String referenceId,
            String referenceType
    ) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            log.warn("Cannot send notification. User not found: {}", userId);
            return;
        }

        // 1. Persist notification record for in-app notification center
        NotificationRecord record = NotificationRecord.builder()
                .user(user)
                .title(title)
                .body(body)
                .notificationType(type)
                .referenceId(referenceId)
                .referenceType(referenceType)
                .isRead(false)
                .build();
        notificationRecordRepository.save(record);
        log.info("Saved in-app notification for user: {} [{}]", user.getEmail(), type);

        // 2. Dispatch Push Notification via FCM
        List<DeviceToken> tokens = deviceTokenRepository.findByUserId(userId);
        if (tokens.isEmpty()) {
            log.debug("No active FCM device tokens registered for user: {}", user.getEmail());
            return;
        }

        for (DeviceToken dt : tokens) {
            dispatchFcmPush(dt.getToken(), title, body, type, referenceId, referenceType);
        }
    }

    private void dispatchFcmPush(
            String fcmToken,
            String title,
            String body,
            NotificationType type,
            String referenceId,
            String referenceType
    ) {
        try {
            if (!FirebaseApp.getApps().isEmpty()) {
                // Real Google Cloud FCM Server Dispatch
                Notification notification = Notification.builder()
                        .setTitle(title)
                        .setBody(body)
                        .build();

                AndroidConfig androidConfig = AndroidConfig.builder()
                        .setPriority(AndroidConfig.Priority.HIGH)
                        .setNotification(AndroidNotification.builder()
                                .setChannelId("samadhanx_civic_channel")
                                .setPriority(AndroidNotification.Priority.MAX)
                                .setDefaultSound(true)
                                .setDefaultVibrateTimings(true)
                                .build())
                        .build();

                Message message = Message.builder()
                        .setToken(fcmToken)
                        .setNotification(notification)
                        .putData("title", title)
                        .putData("body", body)
                        .putData("notificationType", type.name())
                        .putData("referenceId", referenceId != null ? referenceId : "")
                        .putData("referenceType", referenceType != null ? referenceType : "CHALLENGE")
                        .setAndroidConfig(androidConfig)
                        .build();

                String messageId = FirebaseMessaging.getInstance().send(message);
                log.info("Successfully dispatched REAL FCM message to Google Cloud: {} for token: {}...",
                        messageId, fcmToken.length() > 15 ? fcmToken.substring(0, 15) : fcmToken);
            } else {
                log.info("FCM Push Dispatched (Dev Mode) -> DeviceToken: {}... | Title: '{}' | Body: '{}' | Type: {} | Ref: {}:{}",
                        fcmToken.length() > 15 ? fcmToken.substring(0, 15) : fcmToken,
                        title,
                        body,
                        type,
                        referenceType,
                        referenceId);
            }
        } catch (Exception e) {
            log.warn("FCM push delivery failed for token {}: {}", fcmToken, e.getMessage());
        }
    }

    @Override
    public Page<NotificationResponse> getUserNotifications(UUID userId, Pageable pageable) {
        return notificationRecordRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(NotificationResponse::fromEntity);
    }

    @Override
    @Transactional
    public void markAsRead(UUID notificationId, UUID userId) {
        NotificationRecord record = notificationRecordRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification", "id", notificationId));

        if (!record.getUser().getId().equals(userId)) {
            return;
        }

        record.setRead(true);
        record.setReadAt(Instant.now());
        notificationRecordRepository.save(record);
    }

    @Override
    @Transactional
    public void markAllAsRead(UUID userId) {
        notificationRecordRepository.markAllAsRead(userId, Instant.now());
    }

    @Override
    public long getUnreadCount(UUID userId) {
        return notificationRecordRepository.countByUserIdAndIsReadFalse(userId);
    }

    @Override
    public List<com.samadhanx.module.notification.dto.EcosystemActivityFeedItem> getEcosystemActivityFeed(int limit) {
        org.springframework.data.domain.PageRequest pageRequest = org.springframework.data.domain.PageRequest.of(0, Math.min(limit, 50), org.springframework.data.domain.Sort.by("createdAt").descending());
        return notificationRecordRepository.findAll(pageRequest).getContent().stream()
                .map(n -> com.samadhanx.module.notification.dto.EcosystemActivityFeedItem.builder()
                        .id(n.getId())
                        .eventType(n.getNotificationType().name())
                        .title(n.getTitle())
                        .summary(n.getBody())
                        .referenceCode(n.getReferenceId())
                        .referenceType(n.getReferenceType())
                        .actorName(n.getUser() != null ? n.getUser().getFullName() : "System")
                        .timestamp(n.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
    }
}
