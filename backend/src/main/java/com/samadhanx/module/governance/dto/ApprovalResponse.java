package com.samadhanx.module.governance.dto;

import com.samadhanx.module.governance.entity.ApprovalRequest;
import com.samadhanx.module.governance.entity.enums.ApprovalStatus;
import com.samadhanx.module.governance.entity.enums.WorkflowActionType;
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
public class ApprovalResponse {

    private UUID id;
    private WorkflowActionType workflowType;
    private UUID targetEntityId;
    private String targetReferenceCode;

    private UUID requestedByUserId;
    private String requestedByName;

    private UUID reviewedByUserId;
    private String reviewedByName;

    private ApprovalStatus status;
    private String justification;
    private String reviewComments;
    private String previousState;
    private String targetState;

    private Instant reviewedAt;
    private Instant createdAt;

    public static ApprovalResponse fromEntity(ApprovalRequest a) {
        if (a == null) return null;

        String reqName = a.getRequestedBy() != null ? a.getRequestedBy().getFullName() : null;
        UUID reqId = a.getRequestedBy() != null ? a.getRequestedBy().getId() : null;

        String revName = a.getReviewedBy() != null ? a.getReviewedBy().getFullName() : null;
        UUID revId = a.getReviewedBy() != null ? a.getReviewedBy().getId() : null;

        return ApprovalResponse.builder()
                .id(a.getId())
                .workflowType(a.getWorkflowType())
                .targetEntityId(a.getTargetEntityId())
                .targetReferenceCode(a.getTargetReferenceCode())
                .requestedByUserId(reqId)
                .requestedByName(reqName)
                .reviewedByUserId(revId)
                .reviewedByName(revName)
                .status(a.getStatus())
                .justification(a.getJustification())
                .reviewComments(a.getReviewComments())
                .previousState(a.getPreviousState())
                .targetState(a.getTargetState())
                .reviewedAt(a.getReviewedAt())
                .createdAt(a.getCreatedAt())
                .build();
    }
}
