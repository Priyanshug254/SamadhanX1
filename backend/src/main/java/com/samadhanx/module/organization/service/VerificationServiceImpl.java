package com.samadhanx.module.organization.service;

import com.samadhanx.common.exception.BadRequestException;
import com.samadhanx.common.exception.ForbiddenException;
import com.samadhanx.common.exception.ResourceNotFoundException;
import com.samadhanx.module.organization.dto.OrganizationResponse;
import com.samadhanx.module.organization.dto.ReviewVerificationRequest;
import com.samadhanx.module.organization.dto.SubmitVerificationRequest;
import com.samadhanx.module.organization.dto.SupportingDocumentRequest;
import com.samadhanx.module.organization.dto.SuspendOrganizationRequest;
import com.samadhanx.module.organization.dto.VerificationAuditLogResponse;
import com.samadhanx.module.organization.dto.VerificationRequestResponse;
import com.samadhanx.module.organization.entity.Organization;
import com.samadhanx.module.organization.entity.SupportingDocument;
import com.samadhanx.module.organization.entity.VerificationAuditLog;
import com.samadhanx.module.organization.entity.VerificationRequest;
import com.samadhanx.module.organization.entity.enums.VerificationActionType;
import com.samadhanx.module.organization.entity.enums.VerificationStatus;
import com.samadhanx.module.organization.repository.OrganizationMemberRepository;
import com.samadhanx.module.organization.repository.OrganizationRepository;
import com.samadhanx.module.organization.repository.SupportingDocumentRepository;
import com.samadhanx.module.organization.repository.VerificationAuditLogRepository;
import com.samadhanx.module.organization.repository.VerificationRequestRepository;
import com.samadhanx.module.role.entity.RoleName;
import com.samadhanx.module.user.entity.User;
import com.samadhanx.module.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class VerificationServiceImpl implements VerificationService {

    private static final Logger log = LoggerFactory.getLogger(VerificationServiceImpl.class);

    private final VerificationRequestRepository verificationRequestRepository;
    private final SupportingDocumentRepository supportingDocumentRepository;
    private final VerificationAuditLogRepository verificationAuditLogRepository;
    private final OrganizationRepository organizationRepository;
    private final OrganizationMemberRepository organizationMemberRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public VerificationRequestResponse submitVerificationRequest(SubmitVerificationRequest request, UUID submitterUserId) {
        Organization org = organizationRepository.findById(request.getOrganizationId())
                .orElseThrow(() -> new ResourceNotFoundException("Organization", "id", request.getOrganizationId()));

        User submitter = userRepository.findById(submitterUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", submitterUserId));

        if (!organizationMemberRepository.existsByOrganizationIdAndUserId(org.getId(), submitterUserId) &&
                !submitter.hasRole(RoleName.SUPER_ADMIN)) {
            throw new ForbiddenException("You must be an affiliated member of this organization to submit verification");
        }

        if (org.getVerificationStatus() == VerificationStatus.VERIFIED) {
            throw new BadRequestException("This organization is already verified");
        }

        VerificationStatus previousStatus = org.getVerificationStatus();
        VerificationStatus newStatus = VerificationStatus.PENDING_VERIFICATION;

        org.setVerificationStatus(newStatus);
        org.setRejectionReason(null);
        organizationRepository.save(org);

        VerificationRequest vr = VerificationRequest.builder()
                .organization(org)
                .status(newStatus)
                .submittedBy(submitter)
                .submittedAt(Instant.now())
                .build();

        VerificationRequest savedVr = verificationRequestRepository.save(vr);

        // Save documents
        for (SupportingDocumentRequest docReq : request.getDocuments()) {
            SupportingDocument doc = SupportingDocument.builder()
                    .organization(org)
                    .verificationRequest(savedVr)
                    .documentType(docReq.getDocumentType())
                    .documentName(docReq.getDocumentName().trim())
                    .documentUrl(docReq.getDocumentUrl().trim())
                    .uploadedBy(submitter)
                    .uploadedAt(Instant.now())
                    .build();
            supportingDocumentRepository.save(doc);
            savedVr.addDocument(doc);
        }

        // Log audit trail
        createAuditLog(org, savedVr, previousStatus, newStatus, submitter,
                getUserPrimaryRoleName(submitter), VerificationActionType.SUBMITTED,
                "Verification application submitted with " + request.getDocuments().size() + " supporting document(s)");

        log.info("Submitted verification request for organization: {} by user: {}", org.getCode(), submitter.getEmail());
        return VerificationRequestResponse.fromEntity(savedVr);
    }

    @Override
    public Page<VerificationRequestResponse> getVerificationQueue(VerificationStatus status, Pageable pageable) {
        if (status != null) {
            return verificationRequestRepository.findByStatus(status, pageable)
                    .map(VerificationRequestResponse::fromEntity);
        }
        return verificationRequestRepository.findAll(pageable)
                .map(VerificationRequestResponse::fromEntity);
    }

    @Override
    public VerificationRequestResponse getVerificationRequestById(UUID requestId) {
        VerificationRequest vr = verificationRequestRepository.findByIdWithDetails(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Verification Request", "id", requestId));
        return VerificationRequestResponse.fromEntity(vr);
    }

    @Override
    @Transactional
    public VerificationRequestResponse assignReviewer(UUID requestId, UUID reviewerUserId, UUID actionByUserId) {
        VerificationRequest vr = verificationRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Verification Request", "id", requestId));

        User reviewer = userRepository.findById(reviewerUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Reviewer User", "id", reviewerUserId));

        User actionBy = userRepository.findById(actionByUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", actionByUserId));

        validateCanReviewVerifications(actionBy);

        VerificationStatus prevStatus = vr.getStatus();
        vr.setAssignedReviewer(reviewer);
        vr.setStatus(VerificationStatus.UNDER_REVIEW);
        verificationRequestRepository.save(vr);

        Organization org = vr.getOrganization();
        org.setVerificationStatus(VerificationStatus.UNDER_REVIEW);
        organizationRepository.save(org);

        createAuditLog(org, vr, prevStatus, VerificationStatus.UNDER_REVIEW, actionBy,
                getUserPrimaryRoleName(actionBy), VerificationActionType.ASSIGNED_FOR_REVIEW,
                "Assigned verification reviewer: " + reviewer.getFullName() + " (" + reviewer.getEmail() + ")");

        log.info("Assigned reviewer: {} to verification request: {}", reviewer.getEmail(), requestId);
        return VerificationRequestResponse.fromEntity(vr);
    }

    @Override
    @Transactional
    public VerificationRequestResponse reviewVerificationRequest(UUID requestId, ReviewVerificationRequest request, UUID reviewerUserId) {
        VerificationRequest vr = verificationRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Verification Request", "id", requestId));

        User reviewer = userRepository.findById(reviewerUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", reviewerUserId));

        validateCanReviewVerifications(reviewer);

        Organization org = vr.getOrganization();
        VerificationStatus prevStatus = vr.getStatus();
        VerificationStatus targetDecision = request.getDecision();

        if (targetDecision != VerificationStatus.VERIFIED &&
                targetDecision != VerificationStatus.REJECTED &&
                targetDecision != VerificationStatus.UNDER_REVIEW) {
            throw new BadRequestException("Invalid review decision status. Must be VERIFIED, REJECTED, or UNDER_REVIEW");
        }

        if (targetDecision == VerificationStatus.REJECTED && !StringUtils.hasText(request.getRejectionReason())) {
            throw new BadRequestException("Rejection reason is mandatory when rejecting an organization verification");
        }

        Instant now = Instant.now();
        vr.setStatus(targetDecision);
        vr.setReviewerNotes(request.getReviewerNotes() != null ? request.getReviewerNotes().trim() : null);

        VerificationActionType actionType;
        String auditComments;

        if (targetDecision == VerificationStatus.VERIFIED) {
            vr.setResolvedAt(now);
            vr.setRejectionReason(null);

            org.setVerificationStatus(VerificationStatus.VERIFIED);
            org.setVerifiedAt(now);
            org.setVerifiedBy(reviewerUserId);
            org.setRejectionReason(null);

            actionType = VerificationActionType.APPROVED;
            auditComments = "Organization verification approved by " + reviewer.getFullName() +
                    (StringUtils.hasText(request.getReviewerNotes()) ? ". Notes: " + request.getReviewerNotes().trim() : "");
        } else if (targetDecision == VerificationStatus.REJECTED) {
            vr.setResolvedAt(now);
            vr.setRejectionReason(request.getRejectionReason().trim());

            org.setVerificationStatus(VerificationStatus.REJECTED);
            org.setRejectionReason(request.getRejectionReason().trim());

            actionType = VerificationActionType.REJECTED;
            auditComments = "Verification rejected. Reason: " + request.getRejectionReason().trim();
        } else {
            org.setVerificationStatus(VerificationStatus.UNDER_REVIEW);
            actionType = VerificationActionType.ASSIGNED_FOR_REVIEW;
            auditComments = "Application marked under active review" +
                    (StringUtils.hasText(request.getReviewerNotes()) ? ". Notes: " + request.getReviewerNotes().trim() : "");
        }

        verificationRequestRepository.save(vr);
        organizationRepository.save(org);

        createAuditLog(org, vr, prevStatus, targetDecision, reviewer,
                getUserPrimaryRoleName(reviewer), actionType, auditComments);

        log.info("Completed verification review for organization: {} with decision: {}", org.getCode(), targetDecision);
        return VerificationRequestResponse.fromEntity(vr);
    }

    @Override
    @Transactional
    public OrganizationResponse suspendOrganization(UUID orgId, SuspendOrganizationRequest request, UUID actionByUserId) {
        Organization org = organizationRepository.findById(orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Organization", "id", orgId));

        User actionBy = userRepository.findById(actionByUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", actionByUserId));

        if (!actionBy.hasRole(RoleName.SUPER_ADMIN)) {
            throw new ForbiddenException("Only SUPER_ADMIN has authority to suspend verified organizations");
        }

        VerificationStatus prevStatus = org.getVerificationStatus();
        org.setVerificationStatus(VerificationStatus.SUSPENDED);
        org.setRejectionReason("SUSPENDED: " + request.getReason().trim());
        Organization savedOrg = organizationRepository.save(org);

        createAuditLog(org, null, prevStatus, VerificationStatus.SUSPENDED, actionBy,
                getUserPrimaryRoleName(actionBy), VerificationActionType.SUSPENDED,
                "Organization suspended by " + actionBy.getFullName() + ". Reason: " + request.getReason().trim() +
                        (StringUtils.hasText(request.getInternalNotes()) ? ". Internal notes: " + request.getInternalNotes().trim() : ""));

        log.warn("Organization suspended: {} by admin: {}. Reason: {}", org.getCode(), actionBy.getEmail(), request.getReason());
        return OrganizationResponse.fromEntity(savedOrg);
    }

    @Override
    public List<VerificationAuditLogResponse> getAuditLogsForOrganization(UUID orgId) {
        return verificationAuditLogRepository.findByOrganizationIdOrderByCreatedAtDesc(orgId).stream()
                .map(VerificationAuditLogResponse::fromEntity)
                .collect(Collectors.toList());
    }

    private void createAuditLog(
            Organization org,
            VerificationRequest vr,
            VerificationStatus prevStatus,
            VerificationStatus newStatus,
            User actor,
            String actorRole,
            VerificationActionType actionType,
            String comments
    ) {
        VerificationAuditLog auditLog = VerificationAuditLog.builder()
                .organization(org)
                .verificationRequest(vr)
                .previousStatus(prevStatus)
                .newStatus(newStatus)
                .actionBy(actor)
                .actionByRole(actorRole)
                .actionType(actionType)
                .comments(comments)
                .createdAt(Instant.now())
                .build();
        verificationAuditLogRepository.save(auditLog);
    }

    private void validateCanReviewVerifications(User user) {
        if (!user.hasRole(RoleName.SUPER_ADMIN) && !user.hasRole(RoleName.GOVERNMENT_ADMIN)) {
            throw new ForbiddenException("You do not have permission to review institutional verifications");
        }
    }

    private String getUserPrimaryRoleName(User user) {
        return user.getRoles().stream()
                .findFirst()
                .map(r -> r.getName().name())
                .orElse("USER");
    }
}
