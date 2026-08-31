package com.samadhanx.module.organization.dto;

import com.samadhanx.module.organization.entity.OrganizationDomain;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrganizationDomainDto {
    private UUID domainId;
    private String code;
    private String name;
    private boolean primary;

    public static OrganizationDomainDto fromEntity(OrganizationDomain od) {
        if (od == null || od.getDomain() == null) return null;
        return OrganizationDomainDto.builder()
                .domainId(od.getDomain().getId())
                .code(od.getDomain().getCode())
                .name(od.getDomain().getName())
                .primary(od.isPrimary())
                .build();
    }
}
