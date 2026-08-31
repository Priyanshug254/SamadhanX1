package com.samadhanx.module.organization.dto;

import com.samadhanx.module.organization.entity.Domain;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
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
public class DomainResponse {
    private UUID id;
    private String code;
    private String name;
    private String description;
    private boolean active;
    private Instant createdAt;

    public static DomainResponse fromEntity(Domain domain) {
        if (domain == null) return null;
        return DomainResponse.builder()
                .id(domain.getId())
                .code(domain.getCode())
                .name(domain.getName())
                .description(domain.getDescription())
                .active(domain.isActive())
                .createdAt(domain.getCreatedAt())
                .build();
    }
}
