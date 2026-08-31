package com.samadhanx.module.solution.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardSummaryResponse {

    private String role;
    private long openInnovationChallenges;
    private long totalTeams;
    private long totalProposals;
    private long underReviewProposals;
    private long shortlistedProposals;
    private long prototypingProposals;
    private long pilotReadyProposals;
    private long myActiveProjects;
    private long myPendingEvaluations;
    private long activeHackathons;
}
