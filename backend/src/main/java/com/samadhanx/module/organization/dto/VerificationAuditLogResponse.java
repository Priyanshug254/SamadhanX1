package com.samadhanx.module.organization.dto;

import com.samadhanx.module.organization.entity.VerificationAuditLog;
import com.samadhanx.module.organization.entity.enums.VerificationActionType;
import com.samadhanx.module.organization.entity.enums.VerificationStatus;
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
public class VerificationAuditLogResponse {

    private UUID id;
    private UUID organizationId;
    private String organizationName;
    private UUID verificationRequestId;
    private VerificationStatus previousStatus;
    private VerificationStatus newStatus;
    private UUID actionBy;
    private String actionByName;
    private String actionByRole;
    private VerificationActionType actionType;
    private String comments;
    private Instant createdAt;

    public static VerificationAuditLogResponse fromEntity(VerificationAuditLog val) {
        if (val == null) return null;

        String orgName = null;
        UUID orgId = null;
        if (val.getOrganization() != null) {
            orgId = val.getOrganization().getId();
            orgName = val.getOrganization().getName();
        }

        String name = null;
        UUID actId = null;
        if (val.getActionBy() != null) {
            actId = val.getActionBy().getId();
            name = val.getActionBy().getFullName();
        }

        return VerificationAuditLogResponse.builder()
                .id(val.getId())
                .organizationId(orgId)
                .organizationName(orgName)
                .verificationRequestId(val.getVerificationRequest() != null ? val.getVerificationRequest().getId() : null)
                .previousStatus(val.getPreviousStatus())
                .newStatus(val.getNewStatus())
                .actionBy(actId)
                .actionByName(name)
                .actionByRole(val.getActionByRole())
                .actionType(val.getActionType())
                .comments(val.getComments())
                .createdAt(val.getCreatedAt())
                .build();
    }
}
