package com.samadhanx.module.organization.service;

import com.samadhanx.module.organization.dto.FacultyProfileRequest;
import com.samadhanx.module.organization.dto.FacultyProfileResponse;
import com.samadhanx.module.organization.dto.InstitutionalResourceRequest;
import com.samadhanx.module.organization.dto.InstitutionalResourceResponse;
import com.samadhanx.module.organization.dto.UniversityProfileRequest;
import com.samadhanx.module.organization.dto.UniversityProfileResponse;

import java.util.List;
import java.util.UUID;

public interface UniversityService {
    UniversityProfileResponse createOrUpdateUniversityProfile(UUID orgId, UniversityProfileRequest request, UUID currentUserId);
    UniversityProfileResponse getUniversityProfile(UUID orgId);
    InstitutionalResourceResponse addResource(UUID orgId, InstitutionalResourceRequest request, UUID currentUserId);
    List<InstitutionalResourceResponse> getResources(UUID orgId);
    void removeResource(UUID orgId, UUID resourceId, UUID currentUserId);
    FacultyProfileResponse createOrUpdateFacultyProfile(FacultyProfileRequest request, UUID userId);
    FacultyProfileResponse getFacultyProfile(UUID userId);
    List<FacultyProfileResponse> getFacultyProfilesForUniversity(UUID orgId);
}
