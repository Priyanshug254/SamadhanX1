package com.samadhanx.module.organization.service;

import com.samadhanx.module.organization.dto.DepartmentProfileRequest;
import com.samadhanx.module.organization.dto.DepartmentProfileResponse;
import com.samadhanx.module.organization.dto.ProblemCategoryRequest;
import com.samadhanx.module.organization.dto.ProblemCategoryResponse;

import java.util.List;
import java.util.UUID;

public interface DepartmentService {
    DepartmentProfileResponse createOrUpdateDepartmentProfile(UUID orgId, DepartmentProfileRequest request, UUID currentUserId);
    DepartmentProfileResponse getDepartmentProfile(UUID orgId);
    ProblemCategoryResponse addProblemCategory(UUID orgId, ProblemCategoryRequest request, UUID currentUserId);
    List<ProblemCategoryResponse> getProblemCategories(UUID orgId);
    void removeProblemCategory(UUID orgId, UUID categoryId, UUID currentUserId);
}
