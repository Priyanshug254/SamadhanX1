package com.samadhanx.module.organization.service;

import com.samadhanx.module.organization.dto.CreateDomainRequest;
import com.samadhanx.module.organization.dto.DomainResponse;
import com.samadhanx.module.organization.entity.Domain;

import java.util.List;
import java.util.UUID;

public interface DomainService {
    List<DomainResponse> getAllActiveDomains();
    DomainResponse getDomainById(UUID id);
    DomainResponse getDomainByCode(String code);
    DomainResponse createDomain(CreateDomainRequest request);
    Domain findEntityById(UUID id);
    Domain findEntityByCode(String code);
}
