package com.samadhanx.module.organization.service;

import com.samadhanx.common.exception.BadRequestException;
import com.samadhanx.common.exception.ResourceNotFoundException;
import com.samadhanx.module.organization.dto.IndustryProfileRequest;
import com.samadhanx.module.organization.dto.IndustryProfileResponse;
import com.samadhanx.module.organization.entity.IndustryProfile;
import com.samadhanx.module.organization.entity.Organization;
import com.samadhanx.module.organization.entity.enums.OrganizationType;
import com.samadhanx.module.organization.repository.IndustryProfileRepository;
import com.samadhanx.module.organization.repository.OrganizationRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class IndustryServiceImpl implements IndustryService {

    private static final Logger log = LoggerFactory.getLogger(IndustryServiceImpl.class);

    private final IndustryProfileRepository industryProfileRepository;
    private final OrganizationRepository organizationRepository;

    @Override
    @Transactional
    public IndustryProfileResponse createOrUpdateIndustryProfile(UUID orgId, IndustryProfileRequest request, UUID currentUserId) {
        Organization org = organizationRepository.findById(orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Organization", "id", orgId));

        if (org.getOrganizationType() != OrganizationType.INDUSTRY &&
                org.getOrganizationType() != OrganizationType.STARTUP &&
                org.getOrganizationType() != OrganizationType.MSME &&
                org.getOrganizationType() != OrganizationType.CSR &&
                org.getOrganizationType() != OrganizationType.INNOVATION_HUB) {
            throw new BadRequestException("Industry profile can only be attached to INDUSTRY, STARTUP, MSME, CSR, or INNOVATION_HUB organizations");
        }

        IndustryProfile ip = industryProfileRepository.findById(orgId).orElse(null);
        if (ip == null) {
            ip = IndustryProfile.builder()
                    .organization(org)
                    .organizationId(orgId)
                    .registrationNumber(request.getRegistrationNumber() != null ? request.getRegistrationNumber().trim() : null)
                    .dpiitRecognized(request.isDpiitRecognized())
                    .dpiitNumber(request.getDpiitNumber() != null ? request.getDpiitNumber().trim() : null)
                    .companyStage(request.getCompanyStage())
                    .offeringTypes(request.getOfferingTypes() != null ? request.getOfferingTypes().trim() : null)
                    .annualCsrBudgetInr(request.getAnnualCsrBudgetInr())
                    .focusSectors(request.getFocusSectors() != null ? request.getFocusSectors().trim() : null)
                    .build();
        } else {
            if (request.getRegistrationNumber() != null) ip.setRegistrationNumber(request.getRegistrationNumber().trim());
            ip.setDpiitRecognized(request.isDpiitRecognized());
            if (request.getDpiitNumber() != null) ip.setDpiitNumber(request.getDpiitNumber().trim());
            if (request.getCompanyStage() != null) ip.setCompanyStage(request.getCompanyStage());
            if (request.getOfferingTypes() != null) ip.setOfferingTypes(request.getOfferingTypes().trim());
            if (request.getAnnualCsrBudgetInr() != null) ip.setAnnualCsrBudgetInr(request.getAnnualCsrBudgetInr());
            if (request.getFocusSectors() != null) ip.setFocusSectors(request.getFocusSectors().trim());
        }

        IndustryProfile saved = industryProfileRepository.save(ip);
        log.info("Saved industry profile for organization: {}", org.getCode());
        return IndustryProfileResponse.fromEntity(saved);
    }

    @Override
    public IndustryProfileResponse getIndustryProfile(UUID orgId) {
        IndustryProfile ip = industryProfileRepository.findById(orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Industry Profile", "organizationId", orgId));
        return IndustryProfileResponse.fromEntity(ip);
    }
}
