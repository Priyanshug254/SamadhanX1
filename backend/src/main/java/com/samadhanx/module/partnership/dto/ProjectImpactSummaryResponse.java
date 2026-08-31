package com.samadhanx.module.partnership.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectImpactSummaryResponse {

    private UUID proposalId;
    private String proposalTrackingNumber;
    private String proposalTitle;
    private int totalPilotsCount;
    private int totalTargetPopulation;
    private int verifiedMetricsCount;
    private int reportedMetricsCount;
    
    @Builder.Default
    private List<ImpactMetricResponse> metrics = new ArrayList<>();
}
