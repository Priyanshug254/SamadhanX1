package com.samadhanx.module.partnership.dto;

import com.samadhanx.module.organization.entity.enums.OrganizationType;
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
public class PartnerMatchResponse {

    private UUID organizationId;
    private String organizationName;
    private String organizationCode;
    private OrganizationType organizationType;
    private double matchScore; // 0 to 100
    private String matchTier; // EXCELLENT (>=80), GOOD (60-79), MODERATE (40-59), LOW (<40)
    
    @Builder.Default
    private List<String> matchingFactors = new ArrayList<>();

    @Builder.Default
    private List<String> missingCapabilities = new ArrayList<>();

    private boolean canMentor;
    private boolean canFund;
    private boolean canPrototype;
    private boolean canTest;
    private boolean canDeploy;
    private BigDecimal availableBudget;
}
