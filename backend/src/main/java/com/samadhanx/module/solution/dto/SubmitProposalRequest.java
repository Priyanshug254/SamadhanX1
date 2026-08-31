package com.samadhanx.module.solution.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
public class SubmitProposalRequest {

    @Schema(example = "Target Innovation-Required Challenge ID")
    @NotNull(message = "Challenge ID is required")
    private UUID challengeId;

    @Schema(example = "Multidisciplinary Team ID")
    @NotNull(message = "Team ID is required")
    private UUID teamId;

    @Schema(example = "Optional Hackathon ID if submitted as part of an official challenge competition")
    private UUID hackathonId;

    @Schema(example = "Solar-Thermal Clay Nanomembrane Arsenic Remediation System", description = "Proposal Title")
    @NotBlank(message = "Title is required")
    @Size(min = 5, max = 255, message = "Title must be between 5 and 255 characters")
    private String title;

    @Schema(example = "Chandauli deep borewells have dissolved arsenite/arsenate species at 0.082 mg/L, which pass through standard sand-gravel filters.", description = "Comprehensive problem understanding")
    @NotBlank(message = "Problem understanding is required")
    @Size(min = 20, max = 5000, message = "Problem understanding must be between 20 and 5000 characters")
    private String problemUnderstanding;

    @Schema(example = "We propose an in-situ zero-electricity gravity ceramic membrane infused with locally available sintered terracotta clay and iron-oxide nanoparticles.", description = "Proposed novel solution")
    @NotBlank(message = "Proposed solution is required")
    @Size(min = 20, max = 5000, message = "Proposed solution must be between 20 and 5000 characters")
    private String proposedSolution;

    @Schema(example = "90% cheaper than commercial reverse osmosis, requires zero electricity, uses locally sourced terracotta clay, and produces zero brine wastewater.", description = "Innovation and novelty aspects")
    @NotBlank(message = "Innovation & novelty is required")
    private String innovationNovelty;

    @Schema(example = "Two-stage gravity filtration: Stage 1 utilizes solar photocatalytic oxidation to convert As(III) to As(V); Stage 2 utilizes iron-clay nanocomposite candle filters for adsorption.", description = "Technical approach & engineering architecture")
    @NotBlank(message = "Technical approach is required")
    private String technicalApproach;

    @Schema(example = "Provides 2,000 liters of potable water daily per community unit at < 0.02 INR per liter, directly benefiting 1,200 villagers and eradicating waterborne toxicity.", description = "Expected measurable impact")
    @NotBlank(message = "Expected impact is required")
    private String expectedImpact;

    @Schema(example = "Phase 1: Lab candle testing (Month 1-2). Phase 2: Pilot prototype on 1 hand pump (Month 3-4). Phase 3: Scaling to village cluster (Month 5-6).", description = "Milestone implementation plan")
    @NotBlank(message = "Implementation plan is required")
    private String implementationPlan;

    @Schema(example = "University Environmental Nanomaterial Lab, Terracotta sintering kiln, ICP-OES water testing facility.")
    private String requiredResources;

    @Schema(example = "185000.00", description = "Estimated prototype development budget in INR")
    @DecimalMin(value = "0.0", message = "Estimated cost cannot be negative")
    private BigDecimal estimatedCostInr;

    @Schema(example = "Modular candle filter design can be manufactured by local rural potters and scaled across all arsenic-affected districts in Eastern UP and Bihar.")
    private String scalabilityPlan;

    @Schema(example = "Gram Panchayat can maintain filter replacement costs through nominal community water tariffs of 10 INR per household monthly.")
    private String sustainabilityModel;

    @Schema(example = "Candle clogging over time: Mitigated via backwash mechanism and pre-sedimentation chamber.")
    private String riskMitigation;

    @Schema(example = "Bench-scale laboratory unit successfully reduced arsenic concentration from 0.10 mg/L to < 0.003 mg/L in 500-hour continuous test.")
    private String prototypeDescription;

    @Schema(description = "Supporting technical documents and CAD/schematic diagrams")
    @Valid
    private List<ProposalDocumentDto> documents;
}
