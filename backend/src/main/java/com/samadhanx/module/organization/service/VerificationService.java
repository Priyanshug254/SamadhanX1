package com.samadhanx.module.organization.service;

import com.samadhanx.module.organization.dto.OrganizationResponse;
import com.samadhanx.module.organization.dto.ReviewVerificationRequest;
import com.samadhanx.module.organization.dto.SubmitVerificationRequest;
import com.samadhanx.module.organization.dto.SuspendOrganizationRequest;
import com.samadhanx.module.organization.dto.VerificationAuditLogResponse;
import com.samadhanx.module.organization.dto.VerificationRequestResponse;
import com.samadhanx.module.organization.entity.enums.VerificationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface VerificationService {
    VerificationRequestResponse submitVerificationRequest(SubmitVerificationRequest request, UUID submitterUserId);
    Page<VerificationRequestResponse> getVerificationQueue(VerificationStatus status, Pageable pageable);
    VerificationRequestResponse getVerificationRequestById(UUID requestId);
    VerificationRequestResponse assignReviewer(UUID requestId, UUID reviewerUserId, UUID actionByUserId);
    VerificationRequestResponse reviewVerificationRequest(UUID requestId, ReviewVerificationRequest request, UUID reviewerUserId);
    OrganizationResponse suspendOrganization(UUID orgId, SuspendOrganizationRequest request, UUID actionByUserId);
    List<VerificationAuditLogResponse> getAuditLogsForOrganization(UUID orgId);
}
