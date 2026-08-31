package com.samadhanx.module.organization.dto;

import com.samadhanx.module.organization.entity.enums.InstitutionType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UniversityProfileRequest {

    @Schema(example = "U-0500", description = "All India Survey on Higher Education code")
    private String aisheCode;

    @Schema(example = "IIT_NIT_IIIT", description = "Category of higher education institution")
    @NotNull(message = "Institution type is required")
    private InstitutionType institutionType;

    @Schema(example = "A++")
    private String naacGrade;

    @Schema(example = "1-10")
    private String nirfRankRange;

    @Schema(example = "true", description = "Whether the university hosts an active incubation centre")
    private boolean hasIncubationCentre;

    @Schema(example = "IIT BHU Technology Incubation & Hub")
    private String incubationCentreName;

    @Schema(example = "550")
    @Min(value = 0, message = "Faculty count cannot be negative")
    private Integer totalFacultyCount;

    @Schema(example = "7200")
    @Min(value = 0, message = "Student count cannot be negative")
    private Integer totalStudentCount;
}
