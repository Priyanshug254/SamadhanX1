package com.samadhanx.module.partnership.dto;

import com.samadhanx.module.partnership.entity.enums.FundingSupportType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
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
public class SubmitFundingOfferRequest {

    @Schema(example = "123e4567-e89b-12d3-a456-426614174000", description = "Funding Requirement ID")
    @NotNull(message = "Requirement ID is required")
    private UUID requirementId;

    @Schema(example = "123e4567-e89b-12d3-a456-426614174001", description = "Sponsor Organization ID (Industry/CSR/Gov)")
    @NotNull(message = "Sponsor Organization ID is required")
    private UUID sponsorOrganizationId;

    @Schema(example = "350000.00", description = "Offered funding amount in INR")
    @NotNull(message = "Offered amount is required")
    @DecimalMin(value = "1000.00", message = "Offered amount must be at least ₹1,000")
    private BigDecimal offeredAmountInr;

    @Schema(example = "MONETARY_GRANT")
    @NotNull(message = "Support type is required")
    private FundingSupportType supportType;

    @Schema(example = "Disbursement in 2 tranches: 50% upon contract signing, 50% upon successful testing milestone.")
    private String termsAndConditions;
}
