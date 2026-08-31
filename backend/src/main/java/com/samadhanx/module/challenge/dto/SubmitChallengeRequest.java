package com.samadhanx.module.challenge.dto;

import com.samadhanx.module.challenge.entity.enums.SeverityLevel;
import com.samadhanx.module.challenge.entity.enums.SubmitterType;
import com.samadhanx.module.challenge.entity.enums.UrgencyLevel;
import com.samadhanx.module.organization.entity.enums.GovernmentLevel;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubmitChallengeRequest {

    @Schema(example = "Severe Ground Water Arsenic Contamination in Chandauli Village", description = "Concise, descriptive title")
    @NotBlank(message = "Title is required")
    @Size(min = 5, max = 255, message = "Title must be between 5 and 255 characters")
    private String title;

    @Schema(example = "Over 1,200 villagers are experiencing skin lesions and chronic abdominal illnesses due to high arsenic concentrations exceeding 0.08 mg/L in 4 community hand pumps. Immediate filtration technology and alternative clean water required.", description = "Detailed problem description")
    @NotBlank(message = "Description is required")
    @Size(min = 20, max = 5000, message = "Description must be between 20 and 5000 characters")
    private String description;

    @Schema(example = "WATER_SANITATION", description = "Domain code (optional; AI will auto-categorize if omitted)")
    private String domainCode;

    @Schema(example = "Drinking Water Filtration")
    private String subCategory;

    @Schema(example = "CITIZEN", description = "Submitter role/originator")
    private SubmitterType submitterType;

    // ── Geospatial Coordinates ──
    @Schema(example = "25.26380000", description = "Latitude (-90.0 to 90.0)")
    @NotNull(message = "Latitude is required")
    @DecimalMin(value = "-90.0", message = "Latitude must be >= -90.0")
    @DecimalMax(value = "90.0", message = "Latitude must be <= 90.0")
    private BigDecimal latitude;

    @Schema(example = "83.26520000", description = "Longitude (-180.0 to 180.0)")
    @NotNull(message = "Longitude is required")
    @DecimalMin(value = "-180.0", message = "Longitude must be >= -180.0")
    @DecimalMax(value = "180.0", message = "Longitude must be <= 180.0")
    private BigDecimal longitude;

    @Schema(example = "Near Gram Panchayat Bhavan, Main Road")
    private String addressLine;

    @Schema(example = "Mughalsarai Rural")
    private String locality;

    @Schema(example = "Chandauli")
    @NotBlank(message = "District is required")
    private String district;

    @Schema(example = "Uttar Pradesh")
    @NotBlank(message = "State is required")
    private String state;

    @Schema(example = "232101")
    @NotBlank(message = "Pincode is required")
    private String pincode;

    @Schema(example = "PANCHAYAT_PRI", description = "Administrative level responsible")
    private GovernmentLevel jurisdictionLevel;

    // ── Severity & Urgency ──
    @Schema(example = "CRITICAL", description = "LOW, MEDIUM, HIGH, CRITICAL")
    @NotNull(message = "Severity level is required")
    private SeverityLevel severityLevel;

    @Schema(example = "IMMEDIATE", description = "LOW, MEDIUM, HIGH, IMMEDIATE")
    @NotNull(message = "Urgency level is required")
    private UrgencyLevel urgencyLevel;

    @Schema(example = "1200", description = "Estimated population directly impacted")
    @Min(value = 1, message = "Estimated affected population must be at least 1")
    private Integer estimatedAffectedPopulation;

    @Schema(description = "Multimedia proof attachments (photos, lab test reports, videos)")
    @Valid
    private List<AttachmentRequest> attachments;
}
