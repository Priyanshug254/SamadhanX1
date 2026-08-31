package com.samadhanx.module.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiDuplicateAnalysisRequest {
    private String candidateTitle;
    private String candidateDescription;
    private String domainCode;
    private String district;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private List<ExistingChallengeSnippet> existingChallenges;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ExistingChallengeSnippet {
        private UUID id;
        private String trackingNumber;
        private String title;
        private String description;
        private BigDecimal latitude;
        private BigDecimal longitude;
    }
}
