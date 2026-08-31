package com.samadhanx.module.partnership.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GovernmentOversightDashboardResponse {

    // Challenges oversight
    private long totalChallenges;
    private long openChallenges;
    private long departmentalResolvedChallenges;
    private long innovationRequiredChallenges;
    private Map<String, Long> challengesByDomain;
    private Map<String, Long> challengesByStatus;

    // Academic & Higher Education
    private long participatingUniversities;
    private long activeAcademicProjects;
    private long totalStudentInnovators;
    private long totalFacultyMentors;

    // Industry / CSR / Startup Ecosystem
    private long verifiedIndustries;
    private long verifiedStartups;
    private long verifiedMsmes;
    private long verifiedCsrEntities;
    private long verifiedResearchLabs;
    private long verifiedInnovationHubs;

    // Collaboration & Funding Metrics
    private long activeCollaborations;
    private long activeMentorshipEngagements;
    private long totalFundingRequirements;
    private BigDecimal totalApprovedFundingInr;
    private long activeCoDevProjects;

    // Project Pipeline
    private long proposalsSubmitted;
    private long proposalsShortlisted;
    private long proposalsPrototyping;
    private long proposalsTesting;
    private long proposalsPilotReady;
    private long activePilotsCount;
    private long completedPilotsCount;
    private long proposalsDeployed;
    private long techTransfersCount;

    // Measurable Societal Impact
    private long totalPopulationBenefited;
    private long distinctDistrictsCovered;
    private BigDecimal totalWaterSavedLitersPerDay;
    private BigDecimal totalEnergySavedKwh;
}
