package com.samadhanx.module.partnership.dto;

import com.samadhanx.module.partnership.entity.ImpactMetric;
import com.samadhanx.module.partnership.entity.enums.KpiName;
import com.samadhanx.module.partnership.entity.enums.MetricVerificationStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecordImpactMetricRequest {

    @Schema(example = "123e4567-e89b-12d3-a456-426614174000", description = "Proposal ID")
    @NotNull(message = "Proposal ID is required")
    private UUID proposalId;

    @Schema(example = "123e4567-e89b-12d3-a456-426614174001", description = "Optional Pilot Project ID")
    private UUID pilotId;

    @Schema(example = "PEOPLE_BENEFITED")
    @NotNull(message = "KPI name is required")
    private KpiName kpiName;

    @Schema(example = "0.00", description = "Baseline value before intervention")
    @NotNull(message = "Baseline value is required")
    private BigDecimal baselineValue;

    @Schema(example = "3500.00", description = "Target value planned")
    @NotNull(message = "Target value is required")
    private BigDecimal targetValue;

    @Schema(example = "3650.00", description = "Actual measured outcome")
    @NotNull(message = "Actual value is required")
    private BigDecimal actualValue;

    @Schema(example = "Persons", description = "Unit of measure")
    @NotBlank(message = "Unit of measure is required")
    private String unitOfMeasure;

    @Schema(example = "https://docs.samadhanx.org/impact/chiraigaon_census_verification.pdf")
    private String evidenceUrl;

    @Schema(example = "Measured across 5 Anganwadi centers and 2 primary health sub-centers.")
    private String remarks;
}
