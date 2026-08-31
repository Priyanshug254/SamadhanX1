package com.samadhanx.module.organization.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FacultyProfileRequest {

    @Schema(example = "123e4567-e89b-12d3-a456-426614174000", description = "Target University organization ID")
    @NotNull(message = "University organization ID is required")
    private UUID organizationId;

    @Schema(example = "Civil & Environmental Engineering")
    @NotBlank(message = "Department name is required")
    @Size(min = 2, max = 100, message = "Department name must be between 2 and 100 characters")
    private String departmentName;

    @Schema(example = "Professor")
    @NotBlank(message = "Designation is required")
    private String designation;

    @Schema(example = "Ph.D. in Hydrogeology & Watershed Management (IIT Roorkee)")
    private String academicQualification;

    @Schema(example = "Hydrology & Water Quality Engineering")
    @NotBlank(message = "Primary discipline is required")
    private String primaryDiscipline;

    @Schema(example = "Groundwater arsenic remediation, rural bio-sand filtration, AI-based catchment monitoring")
    private String researchAreas;

    @Schema(example = "Patent: Low-cost solar thermal water pasteurization apparatus (2024)")
    private String patentsSummary;

    @Schema(example = "24")
    @Min(value = 0, message = "Publications count cannot be negative")
    private Integer publicationsCount;

    @Schema(example = "18")
    @Min(value = 0, message = "Years of experience cannot be negative")
    private Integer yearsOfExperience;

    @Schema(example = "true", description = "Available to mentor student/multidisciplinary innovation teams")
    private boolean availableForMentorship;
}
