package com.samadhanx.module.organization.dto;

import com.samadhanx.module.organization.entity.enums.CompanyStage;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IndustryProfileRequest {

    @Schema(example = "U72900DL2021PTC123456", description = "CIN, GSTIN, LLPIN or Trust Registration Number")
    private String registrationNumber;

    @Schema(example = "true", description = "Whether recognized by DPIIT Startup India")
    private boolean dpiitRecognized;

    @Schema(example = "DIPP123456")
    private String dpiitNumber;

    @Schema(example = "GROWTH")
    private CompanyStage companyStage;

    @Schema(example = "MENTORSHIP, PROTOTYPING, PILOT_TESTING, FUNDING_CSR", description = "Offerings and capabilities available to innovators")
    private String offeringTypes;

    @Schema(example = "2500000.00", description = "Annual CSR budget allocated in INR")
    @DecimalMin(value = "0.0", message = "CSR budget cannot be negative")
    private BigDecimal annualCsrBudgetInr;

    @Schema(example = "Clean drinking water solutions, renewable rural microgrids, waste-to-energy")
    private String focusSectors;
}
