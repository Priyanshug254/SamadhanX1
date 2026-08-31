package com.samadhanx.module.organization.service;

import com.samadhanx.module.organization.dto.AddMemberRequest;
import com.samadhanx.module.organization.dto.OrganizationMemberResponse;
import com.samadhanx.module.organization.dto.OrganizationResponse;
import com.samadhanx.module.organization.dto.RegisterOrganizationRequest;
import com.samadhanx.module.organization.dto.UpdateOrganizationRequest;
import com.samadhanx.module.organization.entity.Organization;
import com.samadhanx.module.organization.entity.enums.OrganizationType;
import com.samadhanx.module.organization.entity.enums.VerificationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface OrganizationService {
    OrganizationResponse registerOrganization(RegisterOrganizationRequest request, UUID creatorUserId);
    OrganizationResponse getOrganizationById(UUID id);
    OrganizationResponse getOrganizationByCode(String code);
    Page<OrganizationResponse> searchOrganizations(OrganizationType type, VerificationStatus status, String state, String district, Pageable pageable);
    OrganizationResponse updateOrganization(UUID id, UpdateOrganizationRequest request, UUID currentUserId);
    List<OrganizationResponse> getOrganizationsForUser(UUID userId);
    OrganizationMemberResponse addMember(UUID orgId, AddMemberRequest request, UUID currentUserId);
    List<OrganizationMemberResponse> getOrganizationMembers(UUID orgId);
    void removeMember(UUID orgId, UUID targetUserId, UUID currentUserId);
    Organization findEntityById(UUID id);
}
