package com.samadhanx.module.organization.dto;

import com.samadhanx.module.organization.entity.IndustryProfile;
import com.samadhanx.module.organization.entity.enums.CompanyStage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IndustryProfileResponse {

    private UUID organizationId;
    private String registrationNumber;
    private boolean dpiitRecognized;
    private String dpiitNumber;
    private CompanyStage companyStage;
    private String offeringTypes;
    private BigDecimal annualCsrBudgetInr;
    private String focusSectors;

    public static IndustryProfileResponse fromEntity(IndustryProfile ip) {
        if (ip == null) return null;
        return IndustryProfileResponse.builder()
                .organizationId(ip.getOrganizationId())
                .registrationNumber(ip.getRegistrationNumber())
                .dpiitRecognized(ip.isDpiitRecognized())
                .dpiitNumber(ip.getDpiitNumber())
                .companyStage(ip.getCompanyStage())
                .offeringTypes(ip.getOfferingTypes())
                .annualCsrBudgetInr(ip.getAnnualCsrBudgetInr())
                .focusSectors(ip.getFocusSectors())
                .build();
    }
}
