package com.samadhanx.module.solution.dto;

import com.samadhanx.module.solution.entity.ProjectDiscussion;
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
public class PostDiscussionRequest {

    @Schema(example = "Optional Proposal ID")
    private UUID proposalId;

    @Schema(example = "We have completed the CAD model for the terracotta sintering mould. Review requested from Faculty Mentor.")
    @NotBlank(message = "Message is required")
    private String message;

    @Schema(example = "false", description = "true if posted as official Faculty Mentor guidance")
    @com.fasterxml.jackson.annotation.JsonProperty("isMentorGuidance")
    @Builder.Default
    private boolean isMentorGuidance = false;

    @Schema(example = "https://media.samadhanx.org/cad/mould_v1.step")
    private String attachmentUrl;
}
