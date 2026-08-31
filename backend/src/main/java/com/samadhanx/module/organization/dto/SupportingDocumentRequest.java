package com.samadhanx.module.organization.dto;

import com.samadhanx.module.organization.entity.enums.DocumentType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SupportingDocumentRequest {

    @Schema(example = "AISHE_CERTIFICATE")
    @NotNull(message = "Document type is required")
    private DocumentType documentType;

    @Schema(example = "aishe_certificate_2025_2026.pdf")
    @NotBlank(message = "Document name is required")
    private String documentName;

    @Schema(example = "https://documents.samadhanx.org/proofs/aishe_certificate_2025.pdf", description = "Secure file URI or storage path")
    @NotBlank(message = "Document URL is required")
    private String documentUrl;
}
