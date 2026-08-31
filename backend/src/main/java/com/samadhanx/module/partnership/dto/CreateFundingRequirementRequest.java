package com.samadhanx.module.partnership.dto;

import com.samadhanx.module.partnership.entity.FundingRequirement;
import com.samadhanx.module.partnership.entity.enums.FundingCategory;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
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
public class CreateFundingRequirementRequest {

    @Schema(example = "123e4567-e89b-12d3-a456-426614174000", description = "Proposal ID")
    @NotNull(message = "Proposal ID is required")
    private UUID proposalId;

    @Schema(example = "350000.00", description = "Requested amount in INR")
    @NotNull(message = "Requested amount is required")
    @DecimalMin(value = "1000.00", message = "Requested amount must be at least ₹1,000")
    private BigDecimal requestedAmountInr;

    @Schema(example = "Procurement of High-Purity Hydroxyapatite Nanopowder & Automated Ceramic Extrusion Molds")
    @NotBlank(message = "Purpose is required")
    private String purpose;

    @Schema(example = "PROTOTYPING_MATERIAL")
    @NotNull(message = "Category is required")
    private FundingCategory category;

    @Schema(example = "Raw material procurement needed to synthesize 200 prototype filtration candles for pilot field testing.")
    @NotBlank(message = "Justification is required")
    private String justification;

    @Schema(example = "200 verified ceramic nanocomposite candles and laboratory testing logs.")
    private String expectedDeliverables;

    @Schema(example = "3 months (Months 2-4 of Prototyping phase)")
    private String proposedTimeline;
}
