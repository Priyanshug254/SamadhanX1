package com.samadhanx.module.organization.service;

import com.samadhanx.common.exception.ConflictException;
import com.samadhanx.common.exception.ResourceNotFoundException;
import com.samadhanx.module.organization.dto.CreateDomainRequest;
import com.samadhanx.module.organization.dto.DomainResponse;
import com.samadhanx.module.organization.entity.Domain;
import com.samadhanx.module.organization.repository.DomainRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DomainServiceImpl implements DomainService {

    private static final Logger log = LoggerFactory.getLogger(DomainServiceImpl.class);

    private final DomainRepository domainRepository;

    @Override
    public List<DomainResponse> getAllActiveDomains() {
        return domainRepository.findAllByActiveTrue().stream()
                .map(DomainResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public DomainResponse getDomainById(UUID id) {
        Domain domain = findEntityById(id);
        return DomainResponse.fromEntity(domain);
    }

    @Override
    public DomainResponse getDomainByCode(String code) {
        Domain domain = findEntityByCode(code);
        return DomainResponse.fromEntity(domain);
    }

    @Override
    @Transactional
    public DomainResponse createDomain(CreateDomainRequest request) {
        String code = request.getCode().trim().toUpperCase();

        if (domainRepository.existsByCode(code)) {
            throw new ConflictException("Domain code already exists: " + code);
        }

        Domain domain = Domain.builder()
                .code(code)
                .name(request.getName().trim())
                .description(request.getDescription() != null ? request.getDescription().trim() : null)
                .active(true)
                .build();

        Domain savedDomain = domainRepository.save(domain);
        log.info("Created new societal domain: {}", code);
        return DomainResponse.fromEntity(savedDomain);
    }

    @Override
    public Domain findEntityById(UUID id) {
        return domainRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Domain", "id", id));
    }

    @Override
    public Domain findEntityByCode(String code) {
        return domainRepository.findByCode(code.trim().toUpperCase())
                .orElseThrow(() -> new ResourceNotFoundException("Domain", "code", code));
    }
}
