package com.samadhanx.module.solution.dto;

import com.samadhanx.module.solution.entity.Proposal;
import com.samadhanx.module.solution.entity.enums.ProposalStatus;
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
public class ProposalSummaryResponse {

    private UUID id;
    private String trackingNumber;
    private UUID challengeId;
    private String challengeTitle;
    private UUID teamId;
    private String teamName;
    private String homeUniversityName;
    private String title;
    private ProposalStatus status;
    private BigDecimal averageScore;
    private Integer evaluationCount;
    private boolean shortlisted;
    private BigDecimal estimatedCostInr;
    private Instant submittedAt;

    public static ProposalSummaryResponse fromEntity(Proposal p) {
        if (p == null) return null;

        String chTitle = null;
        UUID chId = null;
        if (p.getChallenge() != null) {
            chId = p.getChallenge().getId();
            chTitle = p.getChallenge().getTitle();
        }

        String tName = null;
        String univName = null;
        UUID tId = null;
        if (p.getTeam() != null) {
            tId = p.getTeam().getId();
            tName = p.getTeam().getTeamName();
            if (p.getTeam().getHomeUniversity() != null) {
                univName = p.getTeam().getHomeUniversity().getName();
            }
        }

        return ProposalSummaryResponse.builder()
                .id(p.getId())
                .trackingNumber(p.getTrackingNumber())
                .challengeId(chId)
                .challengeTitle(chTitle)
                .teamId(tId)
                .teamName(tName)
                .homeUniversityName(univName)
                .title(p.getTitle())
                .status(p.getStatus())
                .averageScore(p.getAverageScore())
                .evaluationCount(p.getEvaluationCount())
                .shortlisted(p.isShortlisted())
                .estimatedCostInr(p.getEstimatedCostInr())
                .submittedAt(p.getSubmittedAt())
                .build();
    }
}
