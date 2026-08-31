package com.samadhanx.module.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiChallengeAnalysisRequest {
    private String title;
    private String description;
    private String district;
    private String state;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String claimedDomainCode;
    private Integer estimatedAffectedPopulation;
    private Integer evidenceCount;
}
