package com.samadhanx.module.organization.dto;

import com.samadhanx.module.organization.entity.SupportingDocument;
import com.samadhanx.module.organization.entity.enums.DocumentType;
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
public class SupportingDocumentResponse {

    private UUID id;
    private UUID organizationId;
    private UUID verificationRequestId;
    private DocumentType documentType;
    private String documentName;
    private String documentUrl;
    private UUID uploadedBy;
    private String uploadedByName;
    private Instant uploadedAt;

    public static SupportingDocumentResponse fromEntity(SupportingDocument doc) {
        if (doc == null) return null;

        String uName = null;
        UUID uId = null;
        if (doc.getUploadedBy() != null) {
            uId = doc.getUploadedBy().getId();
            uName = doc.getUploadedBy().getFullName();
        }

        return SupportingDocumentResponse.builder()
                .id(doc.getId())
                .organizationId(doc.getOrganization() != null ? doc.getOrganization().getId() : null)
                .verificationRequestId(doc.getVerificationRequest() != null ? doc.getVerificationRequest().getId() : null)
                .documentType(doc.getDocumentType())
                .documentName(doc.getDocumentName())
                .documentUrl(doc.getDocumentUrl())
                .uploadedBy(uId)
                .uploadedByName(uName)
                .uploadedAt(doc.getUploadedAt())
                .build();
    }
}
