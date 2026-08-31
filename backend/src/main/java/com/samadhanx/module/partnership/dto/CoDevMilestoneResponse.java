package com.samadhanx.module.partnership.dto;

import com.samadhanx.module.partnership.entity.CoDevMilestone;
import com.samadhanx.module.partnership.entity.enums.CoDevMilestoneStatus;
import com.samadhanx.module.partnership.entity.enums.LeadParty;
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
public class CoDevMilestoneResponse {

    private UUID id;
    private UUID projectId;
    private String milestoneName;
    private LeadParty leadParty;
    private String deliverables;
    private Instant dueDate;
    private Instant completionDate;
    private CoDevMilestoneStatus status;
    private String documentationUrl;
    private Instant createdAt;

    public static CoDevMilestoneResponse fromEntity(CoDevMilestone m) {
        if (m == null) return null;
        return CoDevMilestoneResponse.builder()
                .id(m.getId())
                .projectId(m.getProject() != null ? m.getProject().getId() : null)
                .milestoneName(m.getMilestoneName())
                .leadParty(m.getLeadParty())
                .deliverables(m.getDeliverables())
                .dueDate(m.getDueDate())
                .completionDate(m.getCompletionDate())
                .status(m.getStatus())
                .documentationUrl(m.getDocumentationUrl())
                .createdAt(m.getCreatedAt())
                .build();
    }
}
