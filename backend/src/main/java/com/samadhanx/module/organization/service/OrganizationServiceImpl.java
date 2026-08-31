package com.samadhanx.module.organization.service;

import com.samadhanx.common.exception.ConflictException;
import com.samadhanx.common.exception.ForbiddenException;
import com.samadhanx.common.exception.ResourceNotFoundException;
import com.samadhanx.module.organization.dto.AddMemberRequest;
import com.samadhanx.module.organization.dto.OrganizationMemberResponse;
import com.samadhanx.module.organization.dto.OrganizationResponse;
import com.samadhanx.module.organization.dto.RegisterOrganizationRequest;
import com.samadhanx.module.organization.dto.UpdateOrganizationRequest;
import com.samadhanx.module.organization.entity.Domain;
import com.samadhanx.module.organization.entity.Organization;
import com.samadhanx.module.organization.entity.OrganizationMember;
import com.samadhanx.module.organization.entity.enums.OrgMemberRole;
import com.samadhanx.module.organization.entity.enums.OrganizationType;
import com.samadhanx.module.organization.entity.enums.VerificationStatus;
import com.samadhanx.module.organization.repository.DomainRepository;
import com.samadhanx.module.organization.repository.OrganizationDomainRepository;
import com.samadhanx.module.organization.repository.OrganizationMemberRepository;
import com.samadhanx.module.organization.repository.OrganizationRepository;
import com.samadhanx.module.user.entity.User;
import com.samadhanx.module.user.repository.UserRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrganizationServiceImpl implements OrganizationService {

    private static final Logger log = LoggerFactory.getLogger(OrganizationServiceImpl.class);

    private final OrganizationRepository organizationRepository;
    private final OrganizationMemberRepository organizationMemberRepository;
    private final OrganizationDomainRepository organizationDomainRepository;
    private final DomainRepository domainRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public OrganizationResponse registerOrganization(RegisterOrganizationRequest request, UUID creatorUserId) {
        String code = request.getCode().trim().toUpperCase();

        if (organizationRepository.existsByCodeIgnoreCase(code)) {
            throw new ConflictException("Organization with code '" + code + "' already exists");
        }

        User creator = userRepository.findById(creatorUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", creatorUserId));

        Organization organization = Organization.builder()
                .name(request.getName().trim())
                .code(code)
                .organizationType(request.getOrganizationType())
                .description(request.getDescription() != null ? request.getDescription().trim() : null)
                .website(request.getWebsite() != null ? request.getWebsite().trim() : null)
                .contactEmail(request.getContactEmail().trim().toLowerCase())
                .contactPhone(request.getContactPhone() != null ? request.getContactPhone().trim() : null)
                .addressLine(request.getAddressLine() != null ? request.getAddressLine().trim() : null)
                .district(request.getDistrict().trim())
                .state(request.getState().trim())
                .pincode(request.getPincode() != null ? request.getPincode().trim() : null)
                .verificationStatus(VerificationStatus.PENDING_VERIFICATION)
                .build();

        // Attach domains
        if (request.getDomainCodes() != null && !request.getDomainCodes().isEmpty()) {
            for (String domainCode : request.getDomainCodes()) {
                domainRepository.findByCode(domainCode.trim().toUpperCase()).ifPresent(domain -> {
                    boolean isPrimary = domainCode.equalsIgnoreCase(request.getPrimaryDomainCode());
                    organization.addDomain(domain, isPrimary);
                });
            }
        }

        Organization savedOrg = organizationRepository.save(organization);

        // Attach creator as initial HEAD_OF_INSTITUTION / NODAL_OFFICER member
        OrgMemberRole initialRole = (request.getOrganizationType() == OrganizationType.GOVERNMENT_BODY)
                ? OrgMemberRole.NODAL_OFFICER
                : OrgMemberRole.HEAD_OF_INSTITUTION;

        OrganizationMember creatorMember = OrganizationMember.builder()
                .organization(savedOrg)
                .user(creator)
                .orgRole(initialRole)
                .designation("Organization Creator / Primary Representative")
                .verified(true)
                .build();
        organizationMemberRepository.save(creatorMember);

        log.info("Registered new organization: {} ({}) with initial status: PENDING_VERIFICATION", savedOrg.getName(), code);
        return OrganizationResponse.fromEntity(savedOrg);
    }

    @Override
    public OrganizationResponse getOrganizationById(UUID id) {
        Organization organization = findEntityById(id);
        return OrganizationResponse.fromEntity(organization);
    }

    @Override
    public OrganizationResponse getOrganizationByCode(String code) {
        Organization organization = organizationRepository.findByCodeIgnoreCase(code.trim())
                .orElseThrow(() -> new ResourceNotFoundException("Organization", "code", code));
        return OrganizationResponse.fromEntity(organization);
    }

    @Override
    public Page<OrganizationResponse> searchOrganizations(
            OrganizationType type,
            VerificationStatus status,
            String state,
            String district,
            Pageable pageable
    ) {
        Specification<Organization> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (type != null) {
                predicates.add(cb.equal(root.get("organizationType"), type));
            }
            if (status != null) {
                predicates.add(cb.equal(root.get("verificationStatus"), status));
            }
            if (StringUtils.hasText(state)) {
                predicates.add(cb.equal(cb.lower(root.get("state")), state.trim().toLowerCase()));
            }
            if (StringUtils.hasText(district)) {
                predicates.add(cb.equal(cb.lower(root.get("district")), district.trim().toLowerCase()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return organizationRepository.findAll(spec, pageable)
                .map(OrganizationResponse::fromEntity);
    }

    @Override
    @Transactional
    public OrganizationResponse updateOrganization(UUID id, UpdateOrganizationRequest request, UUID currentUserId) {
        Organization org = findEntityById(id);
        validateUserCanManageOrganization(org.getId(), currentUserId);

        if (StringUtils.hasText(request.getName())) org.setName(request.getName().trim());
        if (request.getDescription() != null) org.setDescription(request.getDescription().trim());
        if (request.getWebsite() != null) org.setWebsite(request.getWebsite().trim());
        if (StringUtils.hasText(request.getContactEmail())) org.setContactEmail(request.getContactEmail().trim().toLowerCase());
        if (request.getContactPhone() != null) org.setContactPhone(request.getContactPhone().trim());
        if (request.getAddressLine() != null) org.setAddressLine(request.getAddressLine().trim());
        if (StringUtils.hasText(request.getDistrict())) org.setDistrict(request.getDistrict().trim());
        if (StringUtils.hasText(request.getState())) org.setState(request.getState().trim());
        if (request.getPincode() != null) org.setPincode(request.getPincode().trim());

        // Update domains if provided
        if (request.getDomainCodes() != null) {
            org.getDomainMappings().clear();
            for (String domainCode : request.getDomainCodes()) {
                domainRepository.findByCode(domainCode.trim().toUpperCase()).ifPresent(domain -> {
                    boolean isPrimary = domainCode.equalsIgnoreCase(request.getPrimaryDomainCode());
                    org.addDomain(domain, isPrimary);
                });
            }
        }

        Organization updated = organizationRepository.save(org);
        log.info("Updated organization: {} by user: {}", org.getCode(), currentUserId);
        return OrganizationResponse.fromEntity(updated);
    }

    @Override
    public List<OrganizationResponse> getOrganizationsForUser(UUID userId) {
        return organizationRepository.findByUserId(userId).stream()
                .map(OrganizationResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public OrganizationMemberResponse addMember(UUID orgId, AddMemberRequest request, UUID currentUserId) {
        Organization org = findEntityById(orgId);
        validateUserCanManageOrganization(org.getId(), currentUserId);

        String userEmail = request.getUserEmail().trim().toLowerCase();
        User targetUser = userRepository.findByEmailIgnoreCase(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", userEmail));

        if (organizationMemberRepository.existsByOrganizationIdAndUserId(orgId, targetUser.getId())) {
            throw new ConflictException("User is already a member of this organization");
        }

        OrganizationMember member = OrganizationMember.builder()
                .organization(org)
                .user(targetUser)
                .orgRole(request.getOrgRole())
                .designation(request.getDesignation() != null ? request.getDesignation().trim() : null)
                .identifier(request.getIdentifier() != null ? request.getIdentifier().trim() : null)
                .verified(true)
                .build();

        OrganizationMember saved = organizationMemberRepository.save(member);
        log.info("Added member: {} to organization: {} with role: {}", userEmail, org.getCode(), request.getOrgRole());
        return OrganizationMemberResponse.fromEntity(saved);
    }

    @Override
    public List<OrganizationMemberResponse> getOrganizationMembers(UUID orgId) {
        findEntityById(orgId); // verify exists
        return organizationMemberRepository.findByOrganizationId(orgId).stream()
                .map(OrganizationMemberResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void removeMember(UUID orgId, UUID targetUserId, UUID currentUserId) {
        Organization org = findEntityById(orgId);
        validateUserCanManageOrganization(org.getId(), currentUserId);

        OrganizationMember member = organizationMemberRepository.findByOrganizationIdAndUserId(orgId, targetUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Member", "userId", targetUserId));

        organizationMemberRepository.delete(member);
        log.info("Removed member: {} from organization: {}", targetUserId, org.getCode());
    }

    @Override
    public Organization findEntityById(UUID id) {
        return organizationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Organization", "id", id));
    }

    private void validateUserCanManageOrganization(UUID orgId, UUID userId) {
        User user = userRepository.findByIdWithRoles(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        // SUPER_ADMIN has global management permissions
        if (user.hasRole(com.samadhanx.module.role.entity.RoleName.SUPER_ADMIN)) {
            return;
        }

        // Otherwise, user must be HEAD_OF_INSTITUTION, NODAL_OFFICER, or DEPT_ADMIN of this organization
        OrganizationMember member = organizationMemberRepository.findByOrganizationIdAndUserId(orgId, userId)
                .orElseThrow(() -> new ForbiddenException("You are not a member of this organization"));

        if (member.getOrgRole() != OrgMemberRole.HEAD_OF_INSTITUTION &&
                member.getOrgRole() != OrgMemberRole.NODAL_OFFICER &&
                member.getOrgRole() != OrgMemberRole.DEPT_ADMIN) {
            throw new ForbiddenException("You do not have administrative privileges for this organization");
        }
    }
}
