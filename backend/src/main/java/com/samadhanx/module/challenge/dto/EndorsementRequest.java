package com.samadhanx.module.challenge.dto;

import com.samadhanx.module.challenge.entity.ChallengeEndorsement;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
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
public class EndorsementRequest {

    @Schema(example = "Our family also faces the same water discoloration and bad odor daily.", description = "Optional community impact comment")
    @Size(max = 500, message = "Comment cannot exceed 500 characters")
    private String comment;

    @Schema(example = "true", description = "Whether the endorser is personally affected by this challenge")
    @Builder.Default
    private boolean isAffected = true;
}
