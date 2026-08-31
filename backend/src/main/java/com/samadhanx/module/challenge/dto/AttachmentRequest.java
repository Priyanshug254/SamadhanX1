package com.samadhanx.module.challenge.dto;

import com.samadhanx.module.challenge.entity.ChallengeAttachment;
import com.samadhanx.module.challenge.entity.enums.MediaType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttachmentRequest {

    @Schema(example = "IMAGE")
    @NotNull(message = "Media type is required")
    private MediaType mediaType;

    @Schema(example = "water_contamination_sample_site.jpg")
    @NotBlank(message = "File name is required")
    private String fileName;

    @Schema(example = "https://media.samadhanx.org/evidence/water_sample_01.jpg", description = "Storage URL or public URI")
    @NotBlank(message = "File URL is required")
    private String fileUrl;

    private Long fileSizeBytes;
    private String mimeType;
    private String caption;
    private BigDecimal geoLatitude;
    private BigDecimal geoLongitude;
}
