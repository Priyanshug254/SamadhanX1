package com.samadhanx.module.partnership.dto;

import com.samadhanx.module.partnership.entity.PartnerCapability;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
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
public class PartnerCapabilityRequest {

    @Schema(example = "123e4567-e89b-12d3-a456-426614174000", description = "Organization ID")
    @NotNull(message = "Organization ID is required")
    private UUID organizationId;

    @Schema(example = "Water Purification, Nanotechnology, Cleantech, Rural Sanitation", description = "Sectors of expertise")
    private String sectors;

    @Schema(example = "Ceramic Membranes, Hydroxyapatite Nanocomposites, Solar Distillation", description = "Core technical competencies")
    private String technologies;

    @Schema(example = "Rural potable water, Fluoride and arsenic remediation, Zero-electricity gravity filtration", description = "Strategic areas of interest")
    private String areasOfInterest;

    @Schema(example = "true", description = "Capable of providing senior industry/R&D mentors")
    private boolean mentoringCapability;

    @Schema(example = "true", description = "Capable of providing CSR or project funding/grants")
    private boolean fundingCapability;

    @Schema(example = "true", description = "Equipped with rapid prototyping, CNC, 3D printing or fabrication slots")
    private boolean prototypingCapability;

    @Schema(example = "true", description = "Possesses accredited testing, QA, and validation labs")
    private boolean testingCapability;

    @Schema(example = "true", description = "Possesses field deployment and distribution channels")
    private boolean deploymentCapability;

    @Schema(example = "Uttar Pradesh, Bihar, Madhya Pradesh", description = "Geographic focus areas")
    private String geographicServiceAreas;

    @Schema(example = "5000000.00", description = "Available CSR/Innovation budget in INR")
    private BigDecimal availableResourcesBudget;
}
