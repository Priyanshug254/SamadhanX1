package com.samadhanx.module.governance.dto;

import com.samadhanx.module.governance.entity.WorkItem;
import com.samadhanx.module.governance.entity.enums.WorkItemPriority;
import com.samadhanx.module.governance.entity.enums.WorkItemStatus;
import com.samadhanx.module.governance.entity.enums.WorkItemType;
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
public class WorkItemResponse {

    private UUID id;
    private String title;
    private String description;
    private WorkItemType itemType;
    private WorkItemStatus status;
    private WorkItemPriority priority;

    private UUID assignedToUserId;
    private String assignedToName;
    private String assignedToEmail;

    private UUID createdByUserId;
    private String createdByName;

    private UUID challengeId;
    private String challengeTrackingNumber;

    private UUID proposalId;
    private String proposalTrackingNumber;

    private UUID teamId;
    private UUID pilotId;

    private Instant dueDate;
    private Instant completedAt;
    private String resolutionNotes;
    private boolean overdue;

    private Instant createdAt;
    private Instant updatedAt;

    public static WorkItemResponse fromEntity(WorkItem item) {
        if (item == null) return null;

        String assignedName = item.getAssignedTo() != null ? item.getAssignedTo().getFullName() : null;
        String assignedEmail = item.getAssignedTo() != null ? item.getAssignedTo().getEmail() : null;
        UUID assignedId = item.getAssignedTo() != null ? item.getAssignedTo().getId() : null;

        String createdName = item.getCreatorUser() != null ? item.getCreatorUser().getFullName() : null;
        UUID createdId = item.getCreatorUser() != null ? item.getCreatorUser().getId() : null;

        return WorkItemResponse.builder()
                .id(item.getId())
                .title(item.getTitle())
                .description(item.getDescription())
                .itemType(item.getItemType())
                .status(item.getStatus())
                .priority(item.getPriority())
                .assignedToUserId(assignedId)
                .assignedToName(assignedName)
                .assignedToEmail(assignedEmail)
                .createdByUserId(createdId)
                .createdByName(createdName)
                .challengeId(item.getChallengeId())
                .challengeTrackingNumber(item.getChallengeTrackingNumber())
                .proposalId(item.getProposalId())
                .proposalTrackingNumber(item.getProposalTrackingNumber())
                .teamId(item.getTeamId())
                .pilotId(item.getPilotId())
                .dueDate(item.getDueDate())
                .completedAt(item.getCompletedAt())
                .resolutionNotes(item.getResolutionNotes())
                .overdue(item.isOverdue())
                .createdAt(item.getCreatedAt())
                .updatedAt(item.getUpdatedAt())
                .build();
    }
}
