package com.samadhanx.module.challenge.dto;

import com.samadhanx.module.challenge.entity.Challenge;
import com.samadhanx.module.challenge.entity.enums.ChallengeStatus;
import com.samadhanx.module.challenge.entity.enums.ResolutionPath;
import com.samadhanx.module.challenge.entity.enums.SeverityLevel;
import com.samadhanx.module.challenge.entity.enums.UrgencyLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChallengeSummaryResponse {

    private UUID id;
    private String trackingNumber;
    private String title;
    private String domainCode;
    private String domainName;
    private String district;
    private String state;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private SeverityLevel severityLevel;
    private UrgencyLevel urgencyLevel;
    private BigDecimal priorityScore;
    private Integer endorsementCount;
    private boolean duplicate;
    private ChallengeStatus status;
    private ResolutionPath resolutionPath;
    private String assignedDepartmentName;
    private Instant createdAt;

    public static ChallengeSummaryResponse fromEntity(Challenge c) {
        if (c == null) return null;

        String dCode = null;
        String dName = null;
        if (c.getDomain() != null) {
            dCode = c.getDomain().getCode();
            dName = c.getDomain().getName();
        }

        String deptName = null;
        if (c.getAssignedDepartment() != null && c.getAssignedDepartment().getOrganization() != null) {
            deptName = c.getAssignedDepartment().getOrganization().getName();
        }

        return ChallengeSummaryResponse.builder()
                .id(c.getId())
                .trackingNumber(c.getTrackingNumber())
                .title(c.getTitle())
                .domainCode(dCode)
                .domainName(dName)
                .district(c.getDistrict())
                .state(c.getState())
                .latitude(c.getLatitude())
                .longitude(c.getLongitude())
                .severityLevel(c.getSeverityLevel())
                .urgencyLevel(c.getUrgencyLevel())
                .priorityScore(c.getPriorityScore())
                .endorsementCount(c.getEndorsementCount())
                .duplicate(c.isDuplicate())
                .status(c.getStatus())
                .resolutionPath(c.getResolutionPath())
                .assignedDepartmentName(deptName)
                .createdAt(c.getCreatedAt())
                .build();
    }
}
