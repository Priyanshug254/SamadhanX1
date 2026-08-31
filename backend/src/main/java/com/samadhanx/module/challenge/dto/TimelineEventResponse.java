package com.samadhanx.module.challenge.dto;

import com.samadhanx.module.challenge.entity.ChallengeTimelineEvent;
import com.samadhanx.module.challenge.entity.enums.ChallengeStatus;
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
public class TimelineEventResponse {

    private UUID id;
    private UUID challengeId;
    private ChallengeStatus previousStatus;
    private ChallengeStatus newStatus;
    private UUID actorId;
    private String actorName;
    private String actorRole;
    private String eventTitle;
    private String eventMessage;
    private boolean isPublic;
    private Instant createdAt;

    public static TimelineEventResponse fromEntity(ChallengeTimelineEvent cte) {
        if (cte == null) return null;

        String aName = null;
        UUID aId = null;
        if (cte.getActor() != null) {
            aId = cte.getActor().getId();
            aName = cte.getActor().getFullName();
        }

        return TimelineEventResponse.builder()
                .id(cte.getId())
                .challengeId(cte.getChallenge() != null ? cte.getChallenge().getId() : null)
                .previousStatus(cte.getPreviousStatus())
                .newStatus(cte.getNewStatus())
                .actorId(aId)
                .actorName(aName)
                .actorRole(cte.getActorRole())
                .eventTitle(cte.getEventTitle())
                .eventMessage(cte.getEventMessage())
                .isPublic(cte.isPublic())
                .createdAt(cte.getCreatedAt())
                .build();
    }
}
