package com.samadhanx.module.challenge.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DepartmentResolveRequest {

    @Schema(example = "Installed centralized community reverse-osmosis filtration unit with 500 LPH capacity and connected to all 4 village distribution taps.", description = "Summary of works and resolution measures")
    @NotBlank(message = "Resolution summary is required")
    @Size(min = 10, max = 2000, message = "Resolution summary must be between 10 and 2000 characters")
    private String resolutionSummary;

    @Schema(example = "Arsenic concentration reduced from 0.082 mg/L to < 0.005 mg/L (safe limit: 0.01 mg/L). 1,200 villagers now have continuous access to safe drinking water.", description = "Quantifiable and measurable societal impact")
    private String measurableImpactDescription;
}
