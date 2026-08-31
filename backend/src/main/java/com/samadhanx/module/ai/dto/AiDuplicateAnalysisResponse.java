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
public class AiDuplicateAnalysisResponse {
    private boolean potentialDuplicate;
    private BigDecimal similarityScore; // 0.00 to 1.00
    private UUID matchedParentChallengeId;
    private String matchedTrackingNumber;
    private List<UUID> matchedChallengeIds;
    private String explanation;
    private String modelProvider;
    private boolean fallbackUsed;
}
