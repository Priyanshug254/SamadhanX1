package com.samadhanx.module.notification.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EcosystemEvent {

    private EcosystemEventType eventType;
    private UUID entityId;
    private String trackingNumber;
    private String title;
    private String message;

    private UUID targetUserId;
    private String targetRole;

    private UUID actorUserId;
    private String actorName;

    private Map<String, String> metadata;

    @Builder.Default
    private Instant timestamp = Instant.now();
}
