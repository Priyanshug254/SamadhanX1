package com.samadhanx.module.organization.dto;

import com.samadhanx.module.organization.entity.VerificationRequest;
import com.samadhanx.module.organization.entity.enums.OrganizationType;
import com.samadhanx.module.organization.entity.enums.VerificationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VerificationRequestResponse {

    private UUID id;
    private UUID organizationId;
    private String organizationName;
    private String organizationCode;
    private OrganizationType organizationType;
    private VerificationStatus status;
    private UUID submittedBy;
    private String submittedByName;
    private String submittedByEmail;
    private UUID assignedReviewerId;
    private String assignedReviewerName;
    private String reviewerNotes;
    private String rejectionReason;
    private Instant submittedAt;
    private Instant resolvedAt;
    private List<SupportingDocumentResponse> documents;

    public static VerificationRequestResponse fromEntity(VerificationRequest vr) {
        if (vr == null) return null;

        String orgName = null;
        String orgCode = null;
        OrganizationType orgType = null;
        if (vr.getOrganization() != null) {
            orgName = vr.getOrganization().getName();
            orgCode = vr.getOrganization().getCode();
            orgType = vr.getOrganization().getOrganizationType();
        }

        String subName = null;
        String subEmail = null;
        UUID subId = null;
        if (vr.getSubmittedBy() != null) {
            subId = vr.getSubmittedBy().getId();
            subName = vr.getSubmittedBy().getFullName();
            subEmail = vr.getSubmittedBy().getEmail();
        }

        String revName = null;
        UUID revId = null;
        if (vr.getAssignedReviewer() != null) {
            revId = vr.getAssignedReviewer().getId();
            revName = vr.getAssignedReviewer().getFullName();
        }

        List<SupportingDocumentResponse> docs = null;
        if (vr.getDocuments() != null) {
            docs = vr.getDocuments().stream()
                    .map(SupportingDocumentResponse::fromEntity)
                    .collect(Collectors.toList());
        }

        return VerificationRequestResponse.builder()
                .id(vr.getId())
                .organizationId(vr.getOrganization() != null ? vr.getOrganization().getId() : null)
                .organizationName(orgName)
                .organizationCode(orgCode)
                .organizationType(orgType)
                .status(vr.getStatus())
                .submittedBy(subId)
                .submittedByName(subName)
                .submittedByEmail(subEmail)
                .assignedReviewerId(revId)
                .assignedReviewerName(revName)
                .reviewerNotes(vr.getReviewerNotes())
                .rejectionReason(vr.getRejectionReason())
                .submittedAt(vr.getSubmittedAt())
                .resolvedAt(vr.getResolvedAt())
                .documents(docs)
                .build();
    }
}
