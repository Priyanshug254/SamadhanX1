package com.samadhanx.module.notification.dto;

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
public class EcosystemActivityFeedItem {

    private UUID id;
    private String eventType;
    private String title;
    private String summary;
    private String referenceCode;
    private String referenceType;
    private String actorName;
    private String actorRole;
    private String domain;
    private Instant timestamp;
}
