package com.samadhanx.module.partnership.dto;

import com.samadhanx.module.partnership.entity.ValidationTest;
import com.samadhanx.module.partnership.entity.enums.TestResult;
import com.samadhanx.module.partnership.entity.enums.TestType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubmitValidationTestRequest {

    @Schema(example = "123e4567-e89b-12d3-a456-426614174000", description = "Proposal ID")
    @NotNull(message = "Proposal ID is required")
    private UUID proposalId;

    @Schema(example = "WATER_QUALITY_ANALYSIS")
    @NotNull(message = "Test type is required")
    private TestType testType;

    @Schema(example = "NABL Accredited Water Testing Laboratory, IIT BHU")
    @NotBlank(message = "Test environment is required")
    private String testEnvironment;

    @Schema(example = "Dr. S. K. Roy (NABL Lead Auditor)")
    @NotBlank(message = "Evaluator name is required")
    private String evaluatorName;

    @Schema(example = "Arsenic concentration: reduced from 0.09 mg/L to 0.003 mg/L (IS 10500 compliant: <0.01 mg/L). Fluoride: reduced from 3.2 mg/L to 0.6 mg/L (IS 10500 compliant: <1.0 mg/L). Flow rate: 4.8 L/hr.")
    @NotBlank(message = "Parameters tested are required")
    private String parametersTested;

    @Schema(example = "PASSED")
    @NotNull(message = "Test result is required")
    private TestResult testResult;

    @Schema(example = "Minor initial turbidity during first 5 liters flushing.")
    private String issuesIdentified;

    @Schema(example = "Added pre-wash protocol in user instruction manual.")
    private String correctiveActions;

    @Schema(example = "https://docs.samadhanx.org/tests/nabl_report_jal_2026.pdf")
    private String evidenceDocumentUrl;

    @Schema(example = "Demonstrated excellent heavy metal and fluoride remediation under gravity pressure.")
    private String validationRemarks;
}
