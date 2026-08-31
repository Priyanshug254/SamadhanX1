package com.samadhanx.module.challenge.dto;

import com.samadhanx.module.challenge.entity.Challenge;
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
public class UniversityChallengeMatchResponse {

    private UUID challengeId;
    private String trackingNumber;
    private String title;
    private String description;
    private String domainCode;
    private String domainName;
    private String district;
    private String state;
    private BigDecimal priorityScore;
    private Integer estimatedAffectedPopulation;
    private String escalationJustification;
    private List<String> matchingFacultyExperts;
    private List<String> matchingLabs;
    private double matchScore; // 0.0 to 100.0

    public static UniversityChallengeMatchResponse fromChallenge(
            Challenge c,
            String escalationJustification,
            List<String> matchingFaculty,
            List<String> matchingLabs,
            double matchScore
    ) {
        return UniversityChallengeMatchResponse.builder()
                .challengeId(c.getId())
                .trackingNumber(c.getTrackingNumber())
                .title(c.getTitle())
                .description(c.getDescription())
                .domainCode(c.getDomain() != null ? c.getDomain().getCode() : null)
                .domainName(c.getDomain() != null ? c.getDomain().getName() : null)
                .district(c.getDistrict())
                .state(c.getState())
                .priorityScore(c.getPriorityScore())
                .estimatedAffectedPopulation(c.getEstimatedAffectedPopulation())
                .escalationJustification(escalationJustification)
                .matchingFacultyExperts(matchingFaculty)
                .matchingLabs(matchingLabs)
                .matchScore(matchScore)
                .build();
    }
}
