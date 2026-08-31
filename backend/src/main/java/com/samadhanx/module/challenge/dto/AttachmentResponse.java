package com.samadhanx.module.challenge.dto;

import com.samadhanx.module.challenge.entity.ChallengeAttachment;
import com.samadhanx.module.challenge.entity.enums.MediaType;
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
public class AttachmentResponse {

    private UUID id;
    private UUID challengeId;
    private MediaType mediaType;
    private String fileName;
    private String fileUrl;
    private Long fileSizeBytes;
    private String mimeType;
    private String caption;
    private BigDecimal geoLatitude;
    private BigDecimal geoLongitude;
    private UUID uploadedBy;
    private String uploadedByName;
    private Instant createdAt;

    public static AttachmentResponse fromEntity(ChallengeAttachment att) {
        if (att == null) return null;

        String name = null;
        UUID uId = null;
        if (att.getUploadedBy() != null) {
            uId = att.getUploadedBy().getId();
            name = att.getUploadedBy().getFullName();
        }

        return AttachmentResponse.builder()
                .id(att.getId())
                .challengeId(att.getChallenge() != null ? att.getChallenge().getId() : null)
                .mediaType(att.getMediaType())
                .fileName(att.getFileName())
                .fileUrl(att.getFileUrl())
                .fileSizeBytes(att.getFileSizeBytes())
                .mimeType(att.getMimeType())
                .caption(att.getCaption())
                .geoLatitude(att.getGeoLatitude())
                .geoLongitude(att.getGeoLongitude())
                .uploadedBy(uId)
                .uploadedByName(name)
                .createdAt(att.getCreatedAt())
                .build();
    }
}
