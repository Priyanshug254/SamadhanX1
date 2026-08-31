package com.samadhanx.module.challenge.dto;

import com.samadhanx.module.challenge.entity.ChallengeEndorsement;
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
public class EndorsementResponse {

    private UUID id;
    private UUID challengeId;
    private UUID userId;
    private String userName;
    private String comment;
    @com.fasterxml.jackson.annotation.JsonProperty("isAffected")
    private boolean isAffected;
    private Instant createdAt;

    public static EndorsementResponse fromEntity(ChallengeEndorsement ce) {
        if (ce == null) return null;

        String name = null;
        UUID uId = null;
        if (ce.getUser() != null) {
            uId = ce.getUser().getId();
            name = ce.getUser().getFullName();
        }

        return EndorsementResponse.builder()
                .id(ce.getId())
                .challengeId(ce.getChallenge() != null ? ce.getChallenge().getId() : null)
                .userId(uId)
                .userName(name)
                .comment(ce.getComment())
                .isAffected(ce.isAffected())
                .createdAt(ce.getCreatedAt())
                .build();
    }
}
