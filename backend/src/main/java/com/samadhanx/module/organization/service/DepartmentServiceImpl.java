package com.samadhanx.module.organization.service;

import com.samadhanx.common.exception.BadRequestException;
import com.samadhanx.common.exception.ResourceNotFoundException;
import com.samadhanx.module.organization.dto.DepartmentProfileRequest;
import com.samadhanx.module.organization.dto.DepartmentProfileResponse;
import com.samadhanx.module.organization.dto.ProblemCategoryRequest;
import com.samadhanx.module.organization.dto.ProblemCategoryResponse;
import com.samadhanx.module.organization.entity.Department;
import com.samadhanx.module.organization.entity.DepartmentProblemCategory;
import com.samadhanx.module.organization.entity.Organization;
import com.samadhanx.module.organization.entity.enums.OrganizationType;
import com.samadhanx.module.organization.repository.DepartmentProblemCategoryRepository;
import com.samadhanx.module.organization.repository.DepartmentRepository;
import com.samadhanx.module.organization.repository.OrganizationRepository;
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
public class DepartmentServiceImpl implements DepartmentService {

    private static final Logger log = LoggerFactory.getLogger(DepartmentServiceImpl.class);

    private final DepartmentRepository departmentRepository;
    private final DepartmentProblemCategoryRepository problemCategoryRepository;
    private final OrganizationRepository organizationRepository;

    @Override
    @Transactional
    public DepartmentProfileResponse createOrUpdateDepartmentProfile(UUID orgId, DepartmentProfileRequest request, UUID currentUserId) {
        Organization org = organizationRepository.findById(orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Organization", "id", orgId));

        if (org.getOrganizationType() != OrganizationType.GOVERNMENT_BODY) {
            throw new BadRequestException("Department profile can only be attached to organizations of type GOVERNMENT_BODY");
        }

        Department parentDept = null;
        if (request.getParentDepartmentId() != null) {
            parentDept = departmentRepository.findById(request.getParentDepartmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Parent Department", "id", request.getParentDepartmentId()));
        }

        Department dept = departmentRepository.findById(orgId).orElse(null);
        if (dept == null) {
            dept = Department.builder()
                    .organization(org)
                    .organizationId(orgId)
                    .parentDepartment(parentDept)
                    .level(request.getLevel())
                    .jurisdictionArea(request.getJurisdictionArea().trim())
                    .nodalOfficerName(request.getNodalOfficerName() != null ? request.getNodalOfficerName().trim() : null)
                    .nodalOfficerEmail(request.getNodalOfficerEmail() != null ? request.getNodalOfficerEmail().trim() : null)
                    .nodalOfficerPhone(request.getNodalOfficerPhone() != null ? request.getNodalOfficerPhone().trim() : null)
                    .build();
        } else {
            dept.setParentDepartment(parentDept);
            dept.setLevel(request.getLevel());
            dept.setJurisdictionArea(request.getJurisdictionArea().trim());
            if (request.getNodalOfficerName() != null) dept.setNodalOfficerName(request.getNodalOfficerName().trim());
            if (request.getNodalOfficerEmail() != null) dept.setNodalOfficerEmail(request.getNodalOfficerEmail().trim());
            if (request.getNodalOfficerPhone() != null) dept.setNodalOfficerPhone(request.getNodalOfficerPhone().trim());
        }

        Department saved = departmentRepository.save(dept);
        log.info("Saved department profile for organization: {}", org.getCode());
        return DepartmentProfileResponse.fromEntity(saved);
    }

    @Override
    public DepartmentProfileResponse getDepartmentProfile(UUID orgId) {
        Department dept = departmentRepository.findByIdWithProblemCategories(orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Department Profile", "organizationId", orgId));
        return DepartmentProfileResponse.fromEntity(dept);
    }

    @Override
    @Transactional
    public ProblemCategoryResponse addProblemCategory(UUID orgId, ProblemCategoryRequest request, UUID currentUserId) {
        Department dept = departmentRepository.findById(orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Department Profile", "organizationId", orgId));

        DepartmentProblemCategory category = DepartmentProblemCategory.builder()
                .department(dept)
                .categoryName(request.getCategoryName().trim())
                .description(request.getDescription() != null ? request.getDescription().trim() : null)
                .typicalResolutionDays(request.getTypicalResolutionDays() != null ? request.getTypicalResolutionDays() : 14)
                .build();

        DepartmentProblemCategory saved = problemCategoryRepository.save(category);
        log.info("Added problem category: '{}' to department: {}", category.getCategoryName(), dept.getOrganizationId());
        return ProblemCategoryResponse.fromEntity(saved);
    }

    @Override
    public List<ProblemCategoryResponse> getProblemCategories(UUID orgId) {
        return problemCategoryRepository.findByDepartmentOrganizationId(orgId).stream()
                .map(ProblemCategoryResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void removeProblemCategory(UUID orgId, UUID categoryId, UUID currentUserId) {
        DepartmentProblemCategory category = problemCategoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Problem Category", "id", categoryId));

        if (!category.getDepartment().getOrganizationId().equals(orgId)) {
            throw new BadRequestException("Problem category does not belong to this department");
        }

        problemCategoryRepository.delete(category);
        log.info("Removed problem category: {} from department: {}", categoryId, orgId);
    }
}
