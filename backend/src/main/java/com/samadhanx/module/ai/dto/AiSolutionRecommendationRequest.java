package com.samadhanx.module.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiSolutionRecommendationRequest {
    private UUID challengeId;
    private String trackingNumber;
    private String title;
    private String description;
    private String domainCode;
    private String domainName;
    private String escalationReason;
    private String district;
    private String state;
}
