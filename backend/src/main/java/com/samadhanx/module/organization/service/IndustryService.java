package com.samadhanx.module.organization.service;

import com.samadhanx.module.organization.dto.IndustryProfileRequest;
import com.samadhanx.module.organization.dto.IndustryProfileResponse;

import java.util.UUID;

public interface IndustryService {
    IndustryProfileResponse createOrUpdateIndustryProfile(UUID orgId, IndustryProfileRequest request, UUID currentUserId);
    IndustryProfileResponse getIndustryProfile(UUID orgId);
}
