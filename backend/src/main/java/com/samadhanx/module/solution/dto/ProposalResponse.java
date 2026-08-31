package com.samadhanx.module.solution.dto;

import com.samadhanx.module.solution.entity.Proposal;
import com.samadhanx.module.solution.entity.enums.ProposalStatus;
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
public class ProposalResponse {

    private UUID id;
    private String trackingNumber;
    private UUID challengeId;
    private String challengeTitle;
    private String challengeTrackingNumber;
    private UUID teamId;
    private String teamName;
    private String homeUniversityName;
    private UUID hackathonId;
    private String title;
    private String problemUnderstanding;
    private String proposedSolution;
    private String innovationNovelty;
    private String technicalApproach;
    private String expectedImpact;
    private String implementationPlan;
    private String requiredResources;
    private BigDecimal estimatedCostInr;
    private String scalabilityPlan;
    private String sustainabilityModel;
    private String riskMitigation;
    private String prototypeDescription;
    private ProposalStatus status;
    private BigDecimal averageScore;
    private Integer evaluationCount;
    private boolean shortlisted;
    private String rejectionReason;
    private UUID submittedById;
    private String submittedByName;
    private Instant submittedAt;
    private List<ProposalDocumentDto> documents;
    private List<TeamMemberDto> teamMembers;
    private List<ProposalEvaluationResponse> evaluations;
    private Instant createdAt;
    private Instant updatedAt;

    public static ProposalResponse fromEntity(Proposal p) {
        if (p == null) return null;

        String chTitle = null;
        String chTrack = null;
        UUID chId = null;
        if (p.getChallenge() != null) {
            chId = p.getChallenge().getId();
            chTitle = p.getChallenge().getTitle();
            chTrack = p.getChallenge().getTrackingNumber();
        }

        String tName = null;
        String univName = null;
        UUID tId = null;
        List<TeamMemberDto> members = null;
        if (p.getTeam() != null) {
            tId = p.getTeam().getId();
            tName = p.getTeam().getTeamName();
            if (p.getTeam().getHomeUniversity() != null) {
                univName = p.getTeam().getHomeUniversity().getName();
            }
            if (p.getTeam().getMembers() != null) {
                members = p.getTeam().getMembers().stream()
                        .map(TeamMemberDto::fromEntity)
                        .collect(Collectors.toList());
            }
        }

        String sName = null;
        UUID sId = null;
        if (p.getSubmittedBy() != null) {
            sId = p.getSubmittedBy().getId();
            sName = p.getSubmittedBy().getFullName();
        }

        List<ProposalDocumentDto> docList = null;
        if (p.getDocuments() != null) {
            docList = p.getDocuments().stream()
                    .map(ProposalDocumentDto::fromEntity)
                    .collect(Collectors.toList());
        }

        List<ProposalEvaluationResponse> evalList = null;
        if (p.getEvaluations() != null) {
            evalList = p.getEvaluations().stream()
                    .map(ProposalEvaluationResponse::fromEntity)
                    .collect(Collectors.toList());
        }

        return ProposalResponse.builder()
                .id(p.getId())
                .trackingNumber(p.getTrackingNumber())
                .challengeId(chId)
                .challengeTitle(chTitle)
                .challengeTrackingNumber(chTrack)
                .teamId(tId)
                .teamName(tName)
                .homeUniversityName(univName)
                .hackathonId(p.getHackathonId())
                .title(p.getTitle())
                .problemUnderstanding(p.getProblemUnderstanding())
                .proposedSolution(p.getProposedSolution())
                .innovationNovelty(p.getInnovationNovelty())
                .technicalApproach(p.getTechnicalApproach())
                .expectedImpact(p.getExpectedImpact())
                .implementationPlan(p.getImplementationPlan())
                .requiredResources(p.getRequiredResources())
                .estimatedCostInr(p.getEstimatedCostInr())
                .scalabilityPlan(p.getScalabilityPlan())
                .sustainabilityModel(p.getSustainabilityModel())
                .riskMitigation(p.getRiskMitigation())
                .prototypeDescription(p.getPrototypeDescription())
                .status(p.getStatus())
                .averageScore(p.getAverageScore())
                .evaluationCount(p.getEvaluationCount())
                .shortlisted(p.isShortlisted())
                .rejectionReason(p.getRejectionReason())
                .submittedById(sId)
                .submittedByName(sName)
                .submittedAt(p.getSubmittedAt())
                .documents(docList)
                .teamMembers(members)
                .evaluations(evalList)
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt())
                .build();
    }
}
