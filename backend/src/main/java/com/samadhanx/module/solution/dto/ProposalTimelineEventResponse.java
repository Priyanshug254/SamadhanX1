package com.samadhanx.module.solution.dto;

import com.samadhanx.module.solution.entity.ProposalTimelineEvent;
import com.samadhanx.module.solution.entity.enums.ProposalStatus;
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
public class ProposalTimelineEventResponse {

    private UUID id;
    private UUID proposalId;
    private ProposalStatus previousStatus;
    private ProposalStatus newStatus;
    private UUID actorId;
    private String actorName;
    private String actorRole;
    private String eventTitle;
    private String eventMessage;
    private Instant createdAt;

    public static ProposalTimelineEventResponse fromEntity(ProposalTimelineEvent event) {
        if (event == null) return null;

        String aName = null;
        UUID aId = null;
        if (event.getActor() != null) {
            aId = event.getActor().getId();
            aName = event.getActor().getFullName();
        }

        return ProposalTimelineEventResponse.builder()
                .id(event.getId())
                .proposalId(event.getProposal() != null ? event.getProposal().getId() : null)
                .previousStatus(event.getPreviousStatus())
                .newStatus(event.getNewStatus())
                .actorId(aId)
                .actorName(aName)
                .actorRole(event.getActorRole())
                .eventTitle(event.getEventTitle())
                .eventMessage(event.getEventMessage())
                .createdAt(event.getCreatedAt())
                .build();
    }
}
