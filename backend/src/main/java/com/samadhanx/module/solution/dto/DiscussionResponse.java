package com.samadhanx.module.solution.dto;

import com.samadhanx.module.solution.entity.ProjectDiscussion;
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
public class DiscussionResponse {

    private UUID id;
    private UUID teamId;
    private UUID proposalId;
    private UUID senderId;
    private String senderName;
    private String message;

    @com.fasterxml.jackson.annotation.JsonProperty("isMentorGuidance")
    private boolean isMentorGuidance;

    private String attachmentUrl;
    private Instant createdAt;

    public static DiscussionResponse fromEntity(ProjectDiscussion pd) {
        if (pd == null) return null;

        String sName = null;
        UUID sId = null;
        if (pd.getSender() != null) {
            sId = pd.getSender().getId();
            sName = pd.getSender().getFullName();
        }

        return DiscussionResponse.builder()
                .id(pd.getId())
                .teamId(pd.getTeam() != null ? pd.getTeam().getId() : null)
                .proposalId(pd.getProposal() != null ? pd.getProposal().getId() : null)
                .senderId(sId)
                .senderName(sName)
                .message(pd.getMessage())
                .isMentorGuidance(pd.isMentorGuidance())
                .attachmentUrl(pd.getAttachmentUrl())
                .createdAt(pd.getCreatedAt())
                .build();
    }
}
