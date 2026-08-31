package com.samadhanx.module.challenge.dto;

import com.samadhanx.module.challenge.entity.Challenge;
import com.samadhanx.module.challenge.entity.enums.ChallengeStatus;
import com.samadhanx.module.challenge.entity.enums.ResolutionPath;
import com.samadhanx.module.challenge.entity.enums.SeverityLevel;
import com.samadhanx.module.challenge.entity.enums.SubmitterType;
import com.samadhanx.module.challenge.entity.enums.UrgencyLevel;
import com.samadhanx.module.organization.entity.enums.GovernmentLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChallengeResponse {

    private UUID id;
    private String trackingNumber;
    private String title;
    private String description;

    // Submitter
    private UUID submittedBy;
    private String submitterName;
    private String submitterEmail;
    private SubmitterType submitterType;

    // Domain & AI
    private UUID domainId;
    private String domainCode;
    private String domainName;
    private String subCategory;
    private String aiPredictedDomainCode;
    private BigDecimal aiConfidenceScore;
    private String aiKeywords;
    private String aiReasoning;
    private String aiPriorityReasoning;
    private String aiDuplicateExplanation;
    private String aiSolutionRecommendation;
    private String aiModelProvider;

    // Location
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String addressLine;
    private String locality;
    private String district;
    private String state;
    private String pincode;
    private GovernmentLevel jurisdictionLevel;

    // Priority & Impact
    private SeverityLevel severityLevel;
    private UrgencyLevel urgencyLevel;
    private Integer estimatedAffectedPopulation;
    private BigDecimal priorityScore;
    private Integer endorsementCount;

    // Deduplication
    private UUID clusterId;
    private UUID parentChallengeId;
    private String parentChallengeTrackingNumber;
    private boolean duplicate;
    private BigDecimal duplicateSimilarity;

    // Lifecycle
    private ChallengeStatus status;
    private ResolutionPath resolutionPath;

    // Department Assignment
    private UUID assignedDepartmentId;
    private String assignedDepartmentName;
    private UUID assignedOfficerId;
    private String assignedOfficerName;
    private String routingRationale;
    private Instant targetResolutionDate;

    // Resolution details
    private Instant resolvedAt;
    private String resolutionSummary;
    private String measurableImpactDescription;

    // Sub-collections
    private List<AttachmentResponse> attachments;
    private List<TimelineEventResponse> timeline;
    private List<DepartmentActionResponse> departmentActions;

    private Instant createdAt;
    private Instant updatedAt;

    public static ChallengeResponse fromEntity(Challenge c) {
        if (c == null) return null;

        String sName = null;
        String sEmail = null;
        UUID sId = null;
        if (c.getSubmittedBy() != null) {
            sId = c.getSubmittedBy().getId();
            sName = c.getSubmittedBy().getFullName();
            sEmail = c.getSubmittedBy().getEmail();
        }

        String dCode = null;
        String dName = null;
        UUID dId = null;
        if (c.getDomain() != null) {
            dId = c.getDomain().getId();
            dCode = c.getDomain().getCode();
            dName = c.getDomain().getName();
        }

        String aiPredCode = null;
        if (c.getAiPredictedDomain() != null) {
            aiPredCode = c.getAiPredictedDomain().getCode();
        }

        String deptName = null;
        UUID deptId = null;
        if (c.getAssignedDepartment() != null) {
            deptId = c.getAssignedDepartment().getOrganizationId();
            if (c.getAssignedDepartment().getOrganization() != null) {
                deptName = c.getAssignedDepartment().getOrganization().getName();
            }
        }

        String offName = null;
        UUID offId = null;
        if (c.getAssignedOfficer() != null) {
            offId = c.getAssignedOfficer().getId();
            offName = c.getAssignedOfficer().getFullName();
        }

        String parentTrack = null;
        UUID parentId = null;
        if (c.getParentChallenge() != null) {
            parentId = c.getParentChallenge().getId();
            parentTrack = c.getParentChallenge().getTrackingNumber();
        }

        List<AttachmentResponse> attList = null;
        if (c.getAttachments() != null) {
            attList = c.getAttachments().stream()
                    .map(AttachmentResponse::fromEntity)
                    .collect(Collectors.toList());
        }

        List<TimelineEventResponse> timeList = null;
        if (c.getTimelineEvents() != null) {
            timeList = c.getTimelineEvents().stream()
                    .map(TimelineEventResponse::fromEntity)
                    .collect(Collectors.toList());
        }

        List<DepartmentActionResponse> actList = null;
        if (c.getDepartmentActions() != null) {
            actList = c.getDepartmentActions().stream()
                    .map(DepartmentActionResponse::fromEntity)
                    .collect(Collectors.toList());
        }

        return ChallengeResponse.builder()
                .id(c.getId())
                .trackingNumber(c.getTrackingNumber())
                .title(c.getTitle())
                .description(c.getDescription())
                .submittedBy(sId)
                .submitterName(sName)
                .submitterEmail(sEmail)
                .submitterType(c.getSubmitterType())
                .domainId(dId)
                .domainCode(dCode)
                .domainName(dName)
                .subCategory(c.getSubCategory())
                .aiPredictedDomainCode(aiPredCode)
                .aiConfidenceScore(c.getAiConfidenceScore())
                .aiKeywords(c.getAiKeywords())
                .aiReasoning(c.getAiReasoning())
                .aiPriorityReasoning(c.getAiPriorityReasoning())
                .aiDuplicateExplanation(c.getAiDuplicateExplanation())
                .aiSolutionRecommendation(c.getAiSolutionRecommendation())
                .aiModelProvider(c.getAiModelProvider())
                .latitude(c.getLatitude())
                .longitude(c.getLongitude())
                .addressLine(c.getAddressLine())
                .locality(c.getLocality())
                .district(c.getDistrict())
                .state(c.getState())
                .pincode(c.getPincode())
                .jurisdictionLevel(c.getJurisdictionLevel())
                .severityLevel(c.getSeverityLevel())
                .urgencyLevel(c.getUrgencyLevel())
                .estimatedAffectedPopulation(c.getEstimatedAffectedPopulation())
                .priorityScore(c.getPriorityScore())
                .endorsementCount(c.getEndorsementCount())
                .clusterId(c.getClusterId())
                .parentChallengeId(parentId)
                .parentChallengeTrackingNumber(parentTrack)
                .duplicate(c.isDuplicate())
                .duplicateSimilarity(c.getDuplicateSimilarity())
                .status(c.getStatus())
                .resolutionPath(c.getResolutionPath())
                .assignedDepartmentId(deptId)
                .assignedDepartmentName(deptName)
                .assignedOfficerId(offId)
                .assignedOfficerName(offName)
                .routingRationale(c.getRoutingRationale())
                .targetResolutionDate(c.getTargetResolutionDate())
                .resolvedAt(c.getResolvedAt())
                .resolutionSummary(c.getResolutionSummary())
                .measurableImpactDescription(c.getMeasurableImpactDescription())
                .attachments(attList)
                .timeline(timeList)
                .departmentActions(actList)
                .createdAt(c.getCreatedAt())
                .updatedAt(c.getUpdatedAt())
                .build();
    }
}
