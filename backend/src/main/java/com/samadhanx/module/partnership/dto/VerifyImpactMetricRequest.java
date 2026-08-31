package com.samadhanx.module.partnership.dto;

import com.samadhanx.module.partnership.entity.enums.MetricVerificationStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VerifyImpactMetricRequest {

    @Schema(example = "VERIFIED_BY_GOVERNMENT", description = "Decision: VERIFIED_BY_GOVERNMENT, DISPUTED")
    @NotNull(message = "Verification status is required")
    private MetricVerificationStatus verificationStatus;

    @Schema(example = "District Water & Sanitation Mission field inspection confirmed clean potable water supply.")
    private String remarks;
}
