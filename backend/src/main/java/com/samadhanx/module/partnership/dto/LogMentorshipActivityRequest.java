package com.samadhanx.module.partnership.dto;

import com.samadhanx.module.partnership.entity.MentorshipLog;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
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
public class LogMentorshipActivityRequest {

    @Schema(example = "Sprint Review: Ceramic Sintering Protocol & Porosity Calibration")
    @NotBlank(message = "Session title is required")
    private String sessionTitle;

    @Schema(example = "Reviewed raw sintering results. Recommended heating curve adjustment from 850C to 920C to increase mechanical strength.")
    @NotBlank(message = "Guidance notes are required")
    private String guidanceNotes;

    @Schema(example = "Milestone 2 - Prototype Candle Sintering completed with 95% yield.")
    private String milestonesReviewed;

    @Schema(example = "Conduct 100-hour continuous filtration flux measurement before field testing.")
    private String actionItems;
}
