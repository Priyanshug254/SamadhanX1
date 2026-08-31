package com.samadhanx.module.organization.service;

import com.samadhanx.common.exception.BadRequestException;
import com.samadhanx.common.exception.ConflictException;
import com.samadhanx.common.exception.ResourceNotFoundException;
import com.samadhanx.module.organization.dto.FacultyProfileRequest;
import com.samadhanx.module.organization.dto.FacultyProfileResponse;
import com.samadhanx.module.organization.dto.InstitutionalResourceRequest;
import com.samadhanx.module.organization.dto.InstitutionalResourceResponse;
import com.samadhanx.module.organization.dto.UniversityProfileRequest;
import com.samadhanx.module.organization.dto.UniversityProfileResponse;
import com.samadhanx.module.organization.entity.FacultyProfile;
import com.samadhanx.module.organization.entity.InstitutionalResource;
import com.samadhanx.module.organization.entity.Organization;
import com.samadhanx.module.organization.entity.UniversityProfile;
import com.samadhanx.module.organization.entity.enums.OrganizationType;
import com.samadhanx.module.organization.repository.FacultyProfileRepository;
import com.samadhanx.module.organization.repository.InstitutionalResourceRepository;
import com.samadhanx.module.organization.repository.OrganizationRepository;
import com.samadhanx.module.organization.repository.UniversityProfileRepository;
import com.samadhanx.module.user.entity.User;
import com.samadhanx.module.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UniversityServiceImpl implements UniversityService {

    private static final Logger log = LoggerFactory.getLogger(UniversityServiceImpl.class);

    private final UniversityProfileRepository universityProfileRepository;
    private final InstitutionalResourceRepository institutionalResourceRepository;
    private final FacultyProfileRepository facultyProfileRepository;
    private final OrganizationRepository organizationRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public UniversityProfileResponse createOrUpdateUniversityProfile(UUID orgId, UniversityProfileRequest request, UUID currentUserId) {
        Organization org = organizationRepository.findById(orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Organization", "id", orgId));

        if (org.getOrganizationType() != OrganizationType.UNIVERSITY &&
                org.getOrganizationType() != OrganizationType.RESEARCH_LAB) {
            throw new BadRequestException("University profile can only be attached to UNIVERSITY or RESEARCH_LAB organizations");
        }

        if (StringUtils.hasText(request.getAisheCode())) {
            universityProfileRepository.findByAisheCodeIgnoreCase(request.getAisheCode().trim())
                    .ifPresent(existing -> {
                        if (!existing.getOrganizationId().equals(orgId)) {
                            throw new ConflictException("AISHE code '" + request.getAisheCode() + "' is already registered to another institution");
                        }
                    });
        }

        UniversityProfile up = universityProfileRepository.findById(orgId).orElse(null);
        if (up == null) {
            up = UniversityProfile.builder()
                    .organization(org)
                    .organizationId(orgId)
                    .aisheCode(request.getAisheCode() != null ? request.getAisheCode().trim().toUpperCase() : null)
                    .institutionType(request.getInstitutionType())
                    .naacGrade(request.getNaacGrade() != null ? request.getNaacGrade().trim() : null)
                    .nirfRankRange(request.getNirfRankRange() != null ? request.getNirfRankRange().trim() : null)
                    .hasIncubationCentre(request.isHasIncubationCentre())
                    .incubationCentreName(request.getIncubationCentreName() != null ? request.getIncubationCentreName().trim() : null)
                    .totalFacultyCount(request.getTotalFacultyCount() != null ? request.getTotalFacultyCount() : 0)
                    .totalStudentCount(request.getTotalStudentCount() != null ? request.getTotalStudentCount() : 0)
                    .build();
        } else {
            if (request.getAisheCode() != null) up.setAisheCode(request.getAisheCode().trim().toUpperCase());
            up.setInstitutionType(request.getInstitutionType());
            if (request.getNaacGrade() != null) up.setNaacGrade(request.getNaacGrade().trim());
            if (request.getNirfRankRange() != null) up.setNirfRankRange(request.getNirfRankRange().trim());
            up.setHasIncubationCentre(request.isHasIncubationCentre());
            if (request.getIncubationCentreName() != null) up.setIncubationCentreName(request.getIncubationCentreName().trim());
            if (request.getTotalFacultyCount() != null) up.setTotalFacultyCount(request.getTotalFacultyCount());
            if (request.getTotalStudentCount() != null) up.setTotalStudentCount(request.getTotalStudentCount());
        }

        UniversityProfile saved = universityProfileRepository.save(up);
        log.info("Saved university profile for organization: {}", org.getCode());
        return UniversityProfileResponse.fromEntity(saved);
    }

    @Override
    public UniversityProfileResponse getUniversityProfile(UUID orgId) {
        UniversityProfile up = universityProfileRepository.findById(orgId)
                .orElseThrow(() -> new ResourceNotFoundException("University Profile", "organizationId", orgId));
        return UniversityProfileResponse.fromEntity(up);
    }

    @Override
    @Transactional
    public InstitutionalResourceResponse addResource(UUID orgId, InstitutionalResourceRequest request, UUID currentUserId) {
        Organization org = organizationRepository.findById(orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Organization", "id", orgId));

        InstitutionalResource resource = InstitutionalResource.builder()
                .organization(org)
                .resourceName(request.getResourceName().trim())
                .resourceType(request.getResourceType())
                .description(request.getDescription() != null ? request.getDescription().trim() : null)
                .equipmentList(request.getEquipmentList() != null ? request.getEquipmentList().trim() : null)
                .accessibleToExternalTeams(request.isAccessibleToExternalTeams())
                .build();

        InstitutionalResource saved = institutionalResourceRepository.save(resource);
        log.info("Added resource: '{}' to organization: {}", saved.getResourceName(), org.getCode());
        return InstitutionalResourceResponse.fromEntity(saved);
    }

    @Override
    public List<InstitutionalResourceResponse> getResources(UUID orgId) {
        return institutionalResourceRepository.findByOrganizationId(orgId).stream()
                .map(InstitutionalResourceResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void removeResource(UUID orgId, UUID resourceId, UUID currentUserId) {
        InstitutionalResource resource = institutionalResourceRepository.findById(resourceId)
                .orElseThrow(() -> new ResourceNotFoundException("Resource", "id", resourceId));

        if (!resource.getOrganization().getId().equals(orgId)) {
            throw new BadRequestException("Resource does not belong to this organization");
        }

        institutionalResourceRepository.delete(resource);
        log.info("Removed resource: {} from organization: {}", resourceId, orgId);
    }

    @Override
    @Transactional
    public FacultyProfileResponse createOrUpdateFacultyProfile(FacultyProfileRequest request, UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        Organization org = organizationRepository.findById(request.getOrganizationId())
                .orElseThrow(() -> new ResourceNotFoundException("Organization", "id", request.getOrganizationId()));

        if (org.getOrganizationType() != OrganizationType.UNIVERSITY &&
                org.getOrganizationType() != OrganizationType.RESEARCH_LAB) {
            throw new BadRequestException("Faculty profiles can only be associated with universities or research labs");
        }

        FacultyProfile fp = facultyProfileRepository.findByUserId(userId).orElse(null);
        if (fp == null) {
            fp = FacultyProfile.builder()
                    .user(user)
                    .organization(org)
                    .departmentName(request.getDepartmentName().trim())
                    .designation(request.getDesignation().trim())
                    .academicQualification(request.getAcademicQualification() != null ? request.getAcademicQualification().trim() : null)
                    .primaryDiscipline(request.getPrimaryDiscipline().trim())
                    .researchAreas(request.getResearchAreas() != null ? request.getResearchAreas().trim() : null)
                    .patentsSummary(request.getPatentsSummary() != null ? request.getPatentsSummary().trim() : null)
                    .publicationsCount(request.getPublicationsCount() != null ? request.getPublicationsCount() : 0)
                    .yearsOfExperience(request.getYearsOfExperience() != null ? request.getYearsOfExperience() : 0)
                    .availableForMentorship(request.isAvailableForMentorship())
                    .build();
        } else {
            fp.setOrganization(org);
            fp.setDepartmentName(request.getDepartmentName().trim());
            fp.setDesignation(request.getDesignation().trim());
            if (request.getAcademicQualification() != null) fp.setAcademicQualification(request.getAcademicQualification().trim());
            fp.setPrimaryDiscipline(request.getPrimaryDiscipline().trim());
            if (request.getResearchAreas() != null) fp.setResearchAreas(request.getResearchAreas().trim());
            if (request.getPatentsSummary() != null) fp.setPatentsSummary(request.getPatentsSummary().trim());
            if (request.getPublicationsCount() != null) fp.setPublicationsCount(request.getPublicationsCount());
            if (request.getYearsOfExperience() != null) fp.setYearsOfExperience(request.getYearsOfExperience());
            fp.setAvailableForMentorship(request.isAvailableForMentorship());
        }

        FacultyProfile saved = facultyProfileRepository.save(fp);
        log.info("Saved faculty profile for user: {} with discipline: {}", user.getEmail(), fp.getPrimaryDiscipline());
        return FacultyProfileResponse.fromEntity(saved);
    }

    @Override
    public FacultyProfileResponse getFacultyProfile(UUID userId) {
        FacultyProfile fp = facultyProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Faculty Profile", "userId", userId));
        return FacultyProfileResponse.fromEntity(fp);
    }

    @Override
    public List<FacultyProfileResponse> getFacultyProfilesForUniversity(UUID orgId) {
        return facultyProfileRepository.findByOrganizationIdWithDetails(orgId).stream()
                .map(FacultyProfileResponse::fromEntity)
                .collect(Collectors.toList());
    }
}
