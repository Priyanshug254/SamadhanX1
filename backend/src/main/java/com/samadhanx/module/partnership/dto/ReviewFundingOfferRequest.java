package com.samadhanx.module.partnership.dto;

import com.samadhanx.module.partnership.entity.enums.FundingOfferStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewFundingOfferRequest {

    @Schema(example = "APPROVED", description = "Decision: APPROVED, DISBURSED, UTILIZED, CLOSED")
    @NotNull(message = "Decision status is required")
    private FundingOfferStatus decision;

    @Schema(example = "350000.00", description = "Disbursed amount if updating disbursement")
    private BigDecimal disbursedAmountInr;

    @Schema(example = "Funds utilized for purchasing high-purity hydroxyapatite precursor chemicals and laboratory molds.")
    private String utilizationReport;

    @Schema(example = "https://docs.samadhanx.org/grants/utilization_receipt_01.pdf")
    private String evidenceDocumentUrl;
}
