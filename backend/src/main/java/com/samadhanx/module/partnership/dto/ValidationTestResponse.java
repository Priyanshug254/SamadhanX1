package com.samadhanx.module.partnership.dto;

import com.samadhanx.module.partnership.entity.ValidationTest;
import com.samadhanx.module.partnership.entity.enums.TestResult;
import com.samadhanx.module.partnership.entity.enums.TestType;
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
public class ValidationTestResponse {

    private UUID id;
    private UUID proposalId;
    private String proposalTrackingNumber;
    private String proposalTitle;
    private TestType testType;
    private String testEnvironment;
    private Instant testDate;
    private String evaluatorName;
    private String parametersTested;
    private TestResult testResult;
    private String issuesIdentified;
    private String correctiveActions;
    private String evidenceDocumentUrl;
    private String validationRemarks;
    private UUID createdById;
    private String createdByName;
    private Instant createdAt;

    public static ValidationTestResponse fromEntity(ValidationTest vt) {
        if (vt == null) return null;
        return ValidationTestResponse.builder()
                .id(vt.getId())
                .proposalId(vt.getProposal() != null ? vt.getProposal().getId() : null)
                .proposalTrackingNumber(vt.getProposal() != null ? vt.getProposal().getTrackingNumber() : null)
                .proposalTitle(vt.getProposal() != null ? vt.getProposal().getTitle() : null)
                .testType(vt.getTestType())
                .testEnvironment(vt.getTestEnvironment())
                .testDate(vt.getTestDate())
                .evaluatorName(vt.getEvaluatorName())
                .parametersTested(vt.getParametersTested())
                .testResult(vt.getTestResult())
                .issuesIdentified(vt.getIssuesIdentified())
                .correctiveActions(vt.getCorrectiveActions())
                .evidenceDocumentUrl(vt.getEvidenceDocumentUrl())
                .validationRemarks(vt.getValidationRemarks())
                .createdById(vt.getCreatedBy() != null ? vt.getCreatedBy().getId() : null)
                .createdByName(vt.getCreatedBy() != null ? vt.getCreatedBy().getFullName() : null)
                .createdAt(vt.getCreatedAt())
                .build();
    }
}
