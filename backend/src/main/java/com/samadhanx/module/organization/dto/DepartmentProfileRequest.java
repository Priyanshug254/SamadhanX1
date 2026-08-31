package com.samadhanx.module.organization.dto;

import com.samadhanx.module.organization.entity.enums.GovernmentLevel;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DepartmentProfileRequest {

    @Schema(example = "STATE", description = "Administrative level of the government department")
    @NotNull(message = "Government level is required")
    private GovernmentLevel level;

    @Schema(example = "Uttar Pradesh (All Districts)", description = "Geographical jurisdiction area")
    @NotBlank(message = "Jurisdiction area is required")
    private String jurisdictionArea;

    private UUID parentDepartmentId;

    @Schema(example = "Dr. S. K. Verma")
    private String nodalOfficerName;

    @Schema(example = "nodal.water@up.gov.in")
    private String nodalOfficerEmail;

    @Schema(example = "+915222238000")
    private String nodalOfficerPhone;
}
