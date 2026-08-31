package com.samadhanx.module.solution.dto;

import com.samadhanx.module.solution.entity.Hackathon;
import com.samadhanx.module.solution.entity.enums.HackathonStatus;
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
public class HackathonResponse {

    private UUID id;
    private String title;
    private String code;
    private String description;
    private String bannerUrl;
    private UUID organizerOrgId;
    private String organizerOrgName;
    private UUID domainId;
    private String domainName;
    private Instant submissionDeadline;
    private Instant evaluationDeadline;
    private HackathonStatus status;
    private Integer challengeCount;
    private Integer evaluatorCount;
    private List<HackathonChallengeSummary> challenges;
    private Instant createdAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HackathonChallengeSummary {
        private UUID challengeId;
        private String trackingNumber;
        private String title;
        private String domainName;
    }

    public static HackathonResponse fromEntity(Hackathon h) {
        if (h == null) return null;

        String orgName = null;
        UUID orgId = null;
        if (h.getOrganizerOrganization() != null) {
            orgId = h.getOrganizerOrganization().getId();
            orgName = h.getOrganizerOrganization().getName();
        }

        String dName = null;
        UUID dId = null;
        if (h.getDomain() != null) {
            dId = h.getDomain().getId();
            dName = h.getDomain().getName();
        }

        List<HackathonChallengeSummary> chList = null;
        int cCount = 0;
        if (h.getChallenges() != null) {
            cCount = h.getChallenges().size();
            chList = h.getChallenges().stream()
                    .map(c -> HackathonChallengeSummary.builder()
                            .challengeId(c.getId())
                            .trackingNumber(c.getTrackingNumber())
                            .title(c.getTitle())
                            .domainName(c.getDomain() != null ? c.getDomain().getName() : null)
                            .build())
                    .collect(Collectors.toList());
        }

        int eCount = h.getEvaluators() != null ? h.getEvaluators().size() : 0;

        return HackathonResponse.builder()
                .id(h.getId())
                .title(h.getTitle())
                .code(h.getCode())
                .description(h.getDescription())
                .bannerUrl(h.getBannerUrl())
                .organizerOrgId(orgId)
                .organizerOrgName(orgName)
                .domainId(dId)
                .domainName(dName)
                .submissionDeadline(h.getSubmissionDeadline())
                .evaluationDeadline(h.getEvaluationDeadline())
                .status(h.getStatus())
                .challengeCount(cCount)
                .evaluatorCount(eCount)
                .challenges(chList)
                .createdAt(h.getCreatedAt())
                .build();
    }
}
