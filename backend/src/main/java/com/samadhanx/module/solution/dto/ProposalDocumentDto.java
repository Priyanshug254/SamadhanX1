package com.samadhanx.module.solution.dto;

import com.samadhanx.module.solution.entity.ProposalDocument;
import com.samadhanx.module.solution.entity.enums.ProposalDocumentType;
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
public class ProposalDocumentDto {

    private UUID id;

    @Schema(example = "PROTOTYPE_DIAGRAM", description = "TECHNICAL_SPEC, PROTOTYPE_DIAGRAM, RESEARCH_PAPER, BUDGET_SHEET, CAD_MODEL, OTHER")
    @NotNull(message = "Document type is required")
    private ProposalDocumentType documentType;

    @Schema(example = "clay_nanomembrane_schematic_v2.pdf")
    @NotBlank(message = "Document name is required")
    private String documentName;

    @Schema(example = "https://media.samadhanx.org/proposals/clay_membrane_schematic.pdf")
    @NotBlank(message = "Document URL is required")
    private String documentUrl;

    private UUID uploadedBy;
    private Instant createdAt;

    public static ProposalDocumentDto fromEntity(ProposalDocument doc) {
        if (doc == null) return null;
        return ProposalDocumentDto.builder()
                .id(doc.getId())
                .documentType(doc.getDocumentType())
                .documentName(doc.getDocumentName())
                .documentUrl(doc.getDocumentUrl())
                .uploadedBy(doc.getUploadedBy() != null ? doc.getUploadedBy().getId() : null)
                .createdAt(doc.getCreatedAt())
                .build();
    }
}
