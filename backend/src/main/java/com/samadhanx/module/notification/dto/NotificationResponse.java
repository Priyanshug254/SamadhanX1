package com.samadhanx.module.notification.dto;

import com.samadhanx.module.notification.entity.NotificationRecord;
import com.samadhanx.module.notification.entity.enums.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponse {

    private UUID id;
    private String title;
    private String body;
    private NotificationType notificationType;
    private String referenceId;
    private String referenceType;
    @com.fasterxml.jackson.annotation.JsonProperty("isRead")
    private boolean isRead;
    private Instant readAt;
    private Instant createdAt;

    public static NotificationResponse fromEntity(NotificationRecord entity) {
        if (entity == null) return null;
        return NotificationResponse.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .body(entity.getBody())
                .notificationType(entity.getNotificationType())
                .referenceId(entity.getReferenceId())
                .referenceType(entity.getReferenceType())
                .isRead(entity.isRead())
                .readAt(entity.getReadAt())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
