package com.samadhanx.module.challenge.service;

import com.samadhanx.common.exception.BadRequestException;
import com.samadhanx.common.exception.ConflictException;
import com.samadhanx.common.exception.ForbiddenException;
import com.samadhanx.common.exception.ResourceNotFoundException;
import com.samadhanx.module.challenge.dto.AttachmentRequest;
import com.samadhanx.module.challenge.dto.ChallengeResponse;
import com.samadhanx.module.challenge.dto.ChallengeSummaryResponse;
import com.samadhanx.module.challenge.dto.DepartmentActionRequest;
import com.samadhanx.module.challenge.dto.DepartmentResolveRequest;
import com.samadhanx.module.challenge.dto.EndorsementRequest;
import com.samadhanx.module.challenge.dto.EndorsementResponse;
import com.samadhanx.module.challenge.dto.EscalateToInnovationRequest;
import com.samadhanx.module.challenge.dto.SubmitChallengeRequest;
import com.samadhanx.module.challenge.dto.TimelineEventResponse;
import com.samadhanx.module.challenge.dto.UniversityChallengeMatchResponse;
import com.samadhanx.module.challenge.entity.Challenge;
import com.samadhanx.module.challenge.entity.ChallengeAttachment;
import com.samadhanx.module.challenge.entity.ChallengeDepartmentAction;
import com.samadhanx.module.challenge.entity.ChallengeEndorsement;
import com.samadhanx.module.challenge.entity.ChallengeTimelineEvent;
import com.samadhanx.module.challenge.entity.enums.ChallengeStatus;
import com.samadhanx.module.challenge.entity.enums.DepartmentActionType;
import com.samadhanx.module.challenge.entity.enums.ResolutionPath;
import com.samadhanx.module.challenge.entity.enums.SubmitterType;
import com.samadhanx.module.challenge.repository.ChallengeAttachmentRepository;
import com.samadhanx.module.challenge.repository.ChallengeDepartmentActionRepository;
import com.samadhanx.module.challenge.repository.ChallengeEndorsementRepository;
import com.samadhanx.module.challenge.repository.ChallengeRepository;
import com.samadhanx.module.challenge.repository.ChallengeTimelineEventRepository;
import com.samadhanx.module.organization.entity.Department;
import com.samadhanx.module.organization.entity.Domain;
import com.samadhanx.module.organization.entity.FacultyProfile;
import com.samadhanx.module.organization.entity.InstitutionalResource;
import com.samadhanx.module.organization.entity.Organization;
import com.samadhanx.module.organization.entity.enums.GovernmentLevel;
import com.samadhanx.module.organization.repository.DepartmentRepository;
import com.samadhanx.module.organization.repository.DomainRepository;
import com.samadhanx.module.organization.repository.FacultyProfileRepository;
import com.samadhanx.module.organization.repository.InstitutionalResourceRepository;
import com.samadhanx.module.organization.repository.OrganizationDomainRepository;
import com.samadhanx.module.organization.repository.OrganizationMemberRepository;
import com.samadhanx.module.organization.repository.OrganizationRepository;
import com.samadhanx.module.role.entity.RoleName;
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

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChallengeServiceImpl implements ChallengeService {

    private static final Logger log = LoggerFactory.getLogger(ChallengeServiceImpl.class);
    private static final SecureRandom RANDOM = new SecureRandom();

    private final ChallengeRepository challengeRepository;
    private final ChallengeAttachmentRepository attachmentRepository;
    private final ChallengeEndorsementRepository endorsementRepository;
    private final ChallengeDepartmentActionRepository departmentActionRepository;
    private final ChallengeTimelineEventRepository timelineEventRepository;

    private final DomainRepository domainRepository;
    private final DepartmentRepository departmentRepository;
    private final OrganizationRepository organizationRepository;
    private final OrganizationDomainRepository organizationDomainRepository;
    private final OrganizationMemberRepository organizationMemberRepository;
    private final FacultyProfileRepository facultyProfileRepository;
    private final InstitutionalResourceRepository institutionalResourceRepository;
    private final UserRepository userRepository;

    private final AiCategorizationService aiCategorizationService;
    private final PriorityScoringService priorityScoringService;
    private final DuplicateDetectionService duplicateDetectionService;
    private final DepartmentRoutingEngine departmentRoutingEngine;
    private final com.samadhanx.module.notification.service.PushNotificationService pushNotificationService;
    private final com.samadhanx.module.ai.service.AiIntelligenceService aiIntelligenceService;
    private final com.fasterxml.jackson.databind.ObjectMapper objectMapper;

    @Override
    @Transactional
    public ChallengeResponse submitChallenge(SubmitChallengeRequest request, UUID submitterUserId) {
        User submitter = userRepository.findById(submitterUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", submitterUserId));

        // 1. Resolve / Categorize Domain via AI
        AiCategorizationService.AiCategorizationResult aiResult = aiCategorizationService.categorize(
                request.getTitle(), request.getDescription(), request.getLatitude(), request.getLongitude()
        );

        Domain domain;
        if (StringUtils.hasText(request.getDomainCode())) {
            domain = domainRepository.findByCode(request.getDomainCode().trim().toUpperCase(Locale.ROOT))
                    .orElseGet(() -> domainRepository.findByCode(aiResult.predictedDomainCode())
                            .orElseThrow(() -> new ResourceNotFoundException("Domain", "code", request.getDomainCode())));
        } else {
            domain = domainRepository.findByCode(aiResult.predictedDomainCode())
                    .orElseThrow(() -> new ResourceNotFoundException("Domain", "code", aiResult.predictedDomainCode()));
        }

        Domain aiPredictedDomain = domainRepository.findByCode(aiResult.predictedDomainCode()).orElse(null);

        // 2. Compute Priority Score
        BigDecimal initialPriorityScore = priorityScoringService.computePriorityScore(
                request.getSeverityLevel(),
                request.getUrgencyLevel(),
                request.getEstimatedAffectedPopulation(),
                0
        );

        // 3. Deduplication Check
        DuplicateDetectionService.DuplicateCheckResult dupResult = duplicateDetectionService.checkForDuplicate(
                domain.getId(),
                request.getTitle(),
                request.getDescription(),
                request.getLatitude(),
                request.getLongitude()
        );

        // 4. Automated Department Routing
        GovernmentLevel level = request.getJurisdictionLevel() != null ? request.getJurisdictionLevel() : GovernmentLevel.DISTRICT;
        DepartmentRoutingEngine.DepartmentRoutingResult routingResult = departmentRoutingEngine.findBestMatchingDepartment(
                domain.getId(),
                request.getState().trim(),
                request.getDistrict().trim(),
                level
        );

        Department matchedDept = routingResult.department();
        String routingRationale = routingResult.routingRationale();

        ChallengeStatus initialStatus;
        if (dupResult.isDuplicate()) {
            initialStatus = ChallengeStatus.FLAGGED_DUPLICATE;
        } else if (matchedDept != null) {
            initialStatus = ChallengeStatus.ROUTED_TO_DEPARTMENT;
        } else {
            initialStatus = ChallengeStatus.AI_PROCESSED;
        }

        String trackingNumber = generateTrackingNumber();

        Challenge parentChallenge = null;
        if (dupResult.parentChallengeId() != null) {
            parentChallenge = challengeRepository.findById(dupResult.parentChallengeId()).orElse(null);
        }

        SubmitterType subType = request.getSubmitterType() != null ? request.getSubmitterType() : SubmitterType.CITIZEN;
        String subCategory = StringUtils.hasText(request.getSubCategory()) ? request.getSubCategory().trim() : aiResult.suggestedSubCategory();

        Challenge challenge = Challenge.builder()
                .trackingNumber(trackingNumber)
                .title(request.getTitle().trim())
                .description(request.getDescription().trim())
                .submittedBy(submitter)
                .submitterType(subType)
                .domain(domain)
                .subCategory(subCategory)
                .aiPredictedDomain(aiPredictedDomain)
                .aiConfidenceScore(aiResult.confidenceScore())
                .aiKeywords(String.join(", ", aiResult.extractedKeywords()))
                .aiReasoning(aiResult.reasoning())
                .aiPriorityReasoning("Priority score computed via multi-factor assessment of severity, urgency, population impact, and evidence")
                .aiDuplicateExplanation(dupResult.isDuplicate() ? "Potential duplicate flagged with similarity score " + dupResult.similarityScore() : "Unique challenge verified")
                .aiModelProvider(aiResult.modelProvider())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .addressLine(request.getAddressLine() != null ? request.getAddressLine().trim() : null)
                .locality(request.getLocality() != null ? request.getLocality().trim() : null)
                .district(request.getDistrict().trim())
                .state(request.getState().trim())
                .pincode(request.getPincode().trim())
                .jurisdictionLevel(level)
                .severityLevel(request.getSeverityLevel())
                .urgencyLevel(request.getUrgencyLevel())
                .estimatedAffectedPopulation(request.getEstimatedAffectedPopulation() != null ? request.getEstimatedAffectedPopulation() : 0)
                .priorityScore(initialPriorityScore)
                .endorsementCount(0)
                .clusterId(dupResult.clusterId())
                .parentChallenge(parentChallenge)
                .duplicate(dupResult.isDuplicate())
                .duplicateSimilarity(dupResult.similarityScore())
                .status(initialStatus)
                .resolutionPath(ResolutionPath.PENDING_TRIAGE)
                .assignedDepartment(matchedDept)
                .routingRationale(routingRationale)
                .build();

        Challenge saved = challengeRepository.save(challenge);

        // 5. Save Attachments
        if (request.getAttachments() != null && !request.getAttachments().isEmpty()) {
            for (AttachmentRequest attReq : request.getAttachments()) {
                ChallengeAttachment attachment = ChallengeAttachment.builder()
                        .challenge(saved)
                        .mediaType(attReq.getMediaType())
                        .fileName(attReq.getFileName().trim())
                        .fileUrl(attReq.getFileUrl().trim())
                        .fileSizeBytes(attReq.getFileSizeBytes())
                        .mimeType(attReq.getMimeType())
                        .caption(attReq.getCaption() != null ? attReq.getCaption().trim() : null)
                        .geoLatitude(attReq.getGeoLatitude())
                        .geoLongitude(attReq.getGeoLongitude())
                        .uploadedBy(submitter)
                        .createdAt(Instant.now())
                        .build();
                attachmentRepository.save(attachment);
                saved.addAttachment(attachment);
            }
        }

        // 6. Record Initial Timeline Event
        String routingMsg = matchedDept != null ?
                "Automatically routed to: " + (matchedDept.getOrganization() != null ? matchedDept.getOrganization().getName() : "Local Department") :
                "Categorized under " + domain.getName() + " (Priority: " + initialPriorityScore + "/100)";

        createTimelineEvent(
                saved,
                null,
                initialStatus,
                submitter,
                getUserPrimaryRole(submitter),
                "Challenge Submitted",
                "Challenge registered by " + submitter.getFullName() + ". " + routingMsg,
                true
        );

        log.info("Registered new societal challenge: {} [{}] with status: {}", saved.getTitle(), saved.getTrackingNumber(), saved.getStatus());

        try {
            pushNotificationService.sendNotificationToUser(
                    submitter.getId(),
                    "Challenge Registered [" + saved.getTrackingNumber() + "]",
                    "Your societal challenge '" + saved.getTitle() + "' has been recorded on the civic ledger with priority score " + initialPriorityScore + "/100.",
                    com.samadhanx.module.notification.entity.enums.NotificationType.CHALLENGE_SUBMITTED,
                    saved.getId().toString(),
                    "CHALLENGE"
            );
        } catch (Exception e) {
            log.warn("Failed to dispatch push notification for challenge submission: {}", e.getMessage());
        }

        return ChallengeResponse.fromEntity(saved);
    }

    @Override
    public ChallengeResponse getChallengeById(UUID id) {
        Challenge c = challengeRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Challenge", "id", id));
        return ChallengeResponse.fromEntity(c);
    }

    @Override
    public ChallengeResponse getChallengeByTrackingNumber(String trackingNumber) {
        Challenge c = challengeRepository.findByTrackingNumber(trackingNumber.trim().toUpperCase(Locale.ROOT))
                .orElseThrow(() -> new ResourceNotFoundException("Challenge", "trackingNumber", trackingNumber));
        return ChallengeResponse.fromEntity(c);
    }

    @Override
    public Page<ChallengeSummaryResponse> searchChallenges(
            UUID domainId,
            ChallengeStatus status,
            ResolutionPath resolutionPath,
            String state,
            String district,
            String searchQuery,
            Pageable pageable
    ) {
        Specification<Challenge> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (domainId != null) {
                predicates.add(cb.equal(root.get("domain").get("id"), domainId));
            }
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            if (resolutionPath != null) {
                predicates.add(cb.equal(root.get("resolutionPath"), resolutionPath));
            }
            if (StringUtils.hasText(state)) {
                predicates.add(cb.equal(cb.lower(root.get("state")), state.trim().toLowerCase(Locale.ROOT)));
            }
            if (StringUtils.hasText(district)) {
                predicates.add(cb.equal(cb.lower(root.get("district")), district.trim().toLowerCase(Locale.ROOT)));
            }
            if (StringUtils.hasText(searchQuery)) {
                String searchPattern = "%" + searchQuery.trim().toLowerCase(Locale.ROOT) + "%";
                Predicate titleMatch = cb.like(cb.lower(root.get("title")), searchPattern);
                Predicate descMatch = cb.like(cb.lower(root.get("description")), searchPattern);
                Predicate trackingMatch = cb.like(cb.lower(root.get("trackingNumber")), searchPattern);
                predicates.add(cb.or(titleMatch, descMatch, trackingMatch));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return challengeRepository.findAll(spec, pageable).map(ChallengeSummaryResponse::fromEntity);
    }

    @Override
    public Page<ChallengeSummaryResponse> getMySubmissions(UUID userId, Pageable pageable) {
        return challengeRepository.findBySubmittedById(userId, pageable)
                .map(ChallengeSummaryResponse::fromEntity);
    }

    @Override
    public Page<ChallengeSummaryResponse> getDepartmentAssignedQueue(UUID departmentId, Pageable pageable) {
        return challengeRepository.findByAssignedDepartmentOrganizationId(departmentId, pageable)
                .map(ChallengeSummaryResponse::fromEntity);
    }

    @Override
    @Transactional
    public EndorsementResponse endorseChallenge(UUID challengeId, EndorsementRequest request, UUID userId) {
        Challenge challenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new ResourceNotFoundException("Challenge", "id", challengeId));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        if (endorsementRepository.existsByChallengeIdAndUserId(challengeId, userId)) {
            throw new ConflictException("You have already endorsed this societal challenge");
        }

        ChallengeEndorsement endorsement = ChallengeEndorsement.builder()
                .challenge(challenge)
                .user(user)
                .comment(request != null && request.getComment() != null ? request.getComment().trim() : null)
                .affected(request == null || request.isAffected())
                .createdAt(Instant.now())
                .build();

        ChallengeEndorsement savedEndorsement = endorsementRepository.save(endorsement);
        challenge.addEndorsement(savedEndorsement);

        // Recompute priority score with new endorsement weight
        BigDecimal updatedPriorityScore = priorityScoringService.computePriorityScore(
                challenge.getSeverityLevel(),
                challenge.getUrgencyLevel(),
                challenge.getEstimatedAffectedPopulation(),
                challenge.getEndorsementCount()
        );
        challenge.setPriorityScore(updatedPriorityScore);
        challengeRepository.save(challenge);

        log.info("User {} endorsed challenge {}. New endorsement count: {}, updated priority: {}",
                user.getEmail(), challenge.getTrackingNumber(), challenge.getEndorsementCount(), updatedPriorityScore);

        return EndorsementResponse.fromEntity(savedEndorsement);
    }

    @Override
    @Transactional
    public ChallengeResponse performDepartmentAction(UUID challengeId, DepartmentActionRequest request, UUID officerUserId) {
        Challenge challenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new ResourceNotFoundException("Challenge", "id", challengeId));

        User officer = userRepository.findById(officerUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", officerUserId));

        validateOfficerPermission(challenge, officer);

        ChallengeStatus prevStatus = challenge.getStatus();
        DepartmentActionType actionType = request.getActionType();
        ChallengeStatus newStatus = prevStatus;

        Department dept = challenge.getAssignedDepartment();

        if (actionType == DepartmentActionType.ACCEPTED_FOR_RESOLUTION) {
            newStatus = ChallengeStatus.DEPARTMENT_IN_PROGRESS;
            challenge.setResolutionPath(ResolutionPath.DEPARTMENTAL_STANDARD);
        } else if (actionType == DepartmentActionType.FIELD_INSPECTION_COMPLETED) {
            newStatus = ChallengeStatus.UNDER_DEPARTMENT_TRIAGE;
        } else if (actionType == DepartmentActionType.REQUESTED_CITIZEN_INFO) {
            newStatus = ChallengeStatus.UNDER_DEPARTMENT_TRIAGE;
        } else if (actionType == DepartmentActionType.REJECTED) {
            newStatus = ChallengeStatus.REJECTED;
        } else if (actionType == DepartmentActionType.REASSIGNED) {
            if (request.getReassignedDepartmentId() == null) {
                throw new BadRequestException("Target reassigned department ID is required");
            }
            Department targetDept = departmentRepository.findById(request.getReassignedDepartmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Department", "id", request.getReassignedDepartmentId()));
            challenge.setAssignedDepartment(targetDept);
            dept = targetDept;
            newStatus = ChallengeStatus.ROUTED_TO_DEPARTMENT;
        }

        challenge.setStatus(newStatus);
        challenge.setAssignedOfficer(officer);
        challengeRepository.save(challenge);

        ChallengeDepartmentAction action = ChallengeDepartmentAction.builder()
                .challenge(challenge)
                .department(dept)
                .performedBy(officer)
                .actionType(actionType)
                .fieldInspectionNotes(request.getFieldInspectionNotes() != null ? request.getFieldInspectionNotes().trim() : null)
                .actionNotes(request.getActionNotes() != null ? request.getActionNotes().trim() : null)
                .createdAt(Instant.now())
                .build();

        departmentActionRepository.save(action);
        challenge.addDepartmentAction(action);

        createTimelineEvent(
                challenge,
                prevStatus,
                newStatus,
                officer,
                getUserPrimaryRole(officer),
                "Department Action: " + actionType.name(),
                (StringUtils.hasText(request.getActionNotes()) ? request.getActionNotes().trim() : "Action performed by " + officer.getFullName()),
                true
        );

        log.info("Department action: {} performed on challenge {} by {}", actionType, challenge.getTrackingNumber(), officer.getEmail());
        return ChallengeResponse.fromEntity(challenge);
    }

    @Override
    @Transactional
    public ChallengeResponse resolveDepartmentalStandard(UUID challengeId, DepartmentResolveRequest request, UUID officerUserId) {
        Challenge challenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new ResourceNotFoundException("Challenge", "id", challengeId));

        User officer = userRepository.findById(officerUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", officerUserId));

        validateOfficerPermission(challenge, officer);

        ChallengeStatus prevStatus = challenge.getStatus();
        challenge.setStatus(ChallengeStatus.RESOLVED_BY_DEPARTMENT);
        challenge.setResolutionPath(ResolutionPath.DEPARTMENTAL_STANDARD);
        challenge.setResolvedAt(Instant.now());
        challenge.setResolutionSummary(request.getResolutionSummary().trim());
        challenge.setMeasurableImpactDescription(request.getMeasurableImpactDescription() != null ? request.getMeasurableImpactDescription().trim() : null);
        challenge.setAssignedOfficer(officer);

        challengeRepository.save(challenge);

        ChallengeDepartmentAction action = ChallengeDepartmentAction.builder()
                .challenge(challenge)
                .department(challenge.getAssignedDepartment())
                .performedBy(officer)
                .actionType(DepartmentActionType.RESOLVED)
                .actionNotes("Standard departmental resolution: " + request.getResolutionSummary().trim())
                .createdAt(Instant.now())
                .build();
        departmentActionRepository.save(action);
        challenge.addDepartmentAction(action);

        createTimelineEvent(
                challenge,
                prevStatus,
                ChallengeStatus.RESOLVED_BY_DEPARTMENT,
                officer,
                getUserPrimaryRole(officer),
                "Resolved by Department",
                "Department completed resolution: " + request.getResolutionSummary().trim(),
                true
        );

        log.info("Challenge {} marked RESOLVED_BY_DEPARTMENT by {}", challenge.getTrackingNumber(), officer.getEmail());

        try {
            if (challenge.getSubmittedBy() != null) {
                pushNotificationService.sendNotificationToUser(
                        challenge.getSubmittedBy().getId(),
                        "Challenge Resolved [" + challenge.getTrackingNumber() + "]",
                        "Department has completed resolution: " + request.getResolutionSummary().trim(),
                        com.samadhanx.module.notification.entity.enums.NotificationType.CHALLENGE_RESOLVED,
                        challenge.getId().toString(),
                        "CHALLENGE"
                );
            }
        } catch (Exception e) {
            log.warn("Failed to dispatch push notification for challenge resolution: {}", e.getMessage());
        }

        return ChallengeResponse.fromEntity(challenge);
    }

    @Override
    @Transactional
    public ChallengeResponse escalateToInnovation(UUID challengeId, EscalateToInnovationRequest request, UUID officerUserId) {
        Challenge challenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new ResourceNotFoundException("Challenge", "id", challengeId));

        User officer = userRepository.findById(officerUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", officerUserId));

        validateOfficerPermission(challenge, officer);

        ChallengeStatus prevStatus = challenge.getStatus();
        challenge.setStatus(ChallengeStatus.INNOVATION_REQUIRED);
        challenge.setResolutionPath(ResolutionPath.INNOVATION_RESEARCH);

        try {
            com.samadhanx.module.ai.dto.AiSolutionRecommendationResponse rec = aiIntelligenceService.generateSolutionRecommendation(
                    com.samadhanx.module.ai.dto.AiSolutionRecommendationRequest.builder()
                            .challengeId(challenge.getId())
                            .trackingNumber(challenge.getTrackingNumber())
                            .title(challenge.getTitle())
                            .description(challenge.getDescription())
                            .domainCode(challenge.getDomain() != null ? challenge.getDomain().getCode() : null)
                            .domainName(challenge.getDomain() != null ? challenge.getDomain().getName() : null)
                            .escalationReason(request.getEscalationJustification())
                            .district(challenge.getDistrict())
                            .state(challenge.getState())
                            .build()
            );
            if (rec != null) {
                challenge.setAiSolutionRecommendation(objectMapper.writeValueAsString(rec));
            }
        } catch (Exception e) {
            log.warn("Failed to generate AI solution recommendation: {}", e.getMessage());
        }

        challengeRepository.save(challenge);

        ChallengeDepartmentAction action = ChallengeDepartmentAction.builder()
                .challenge(challenge)
                .department(challenge.getAssignedDepartment())
                .performedBy(officer)
                .actionType(DepartmentActionType.ESCALATED_FOR_INNOVATION)
                .escalationJustification(request.getEscalationJustification().trim())
                .actionNotes(request.getSuggestedCapabilities() != null ? "Suggested Capabilities: " + request.getSuggestedCapabilities().trim() : null)
                .createdAt(Instant.now())
                .build();
        departmentActionRepository.save(action);
        challenge.addDepartmentAction(action);

        createTimelineEvent(
                challenge,
                prevStatus,
                ChallengeStatus.INNOVATION_REQUIRED,
                officer,
                getUserPrimaryRole(officer),
                "Escalated to Academic Innovation Ecosystem",
                "Department flagged challenge for novel technology / R&D solution: " + request.getEscalationJustification().trim(),
                true
        );

        log.info("Challenge {} escalated to INNOVATION_REQUIRED by {}", challenge.getTrackingNumber(), officer.getEmail());

        try {
            if (challenge.getSubmittedBy() != null) {
                pushNotificationService.sendNotificationToUser(
                        challenge.getSubmittedBy().getId(),
                        "Escalated to Innovation Pipeline [" + challenge.getTrackingNumber() + "]",
                        "Your challenge has been escalated to university research hubs: " + request.getEscalationJustification().trim(),
                        com.samadhanx.module.notification.entity.enums.NotificationType.INNOVATION_REQUIRED,
                        challenge.getId().toString(),
                        "CHALLENGE"
                );
            }
        } catch (Exception e) {
            log.warn("Failed to dispatch push notification for innovation escalation: {}", e.getMessage());
        }

        return ChallengeResponse.fromEntity(challenge);
    }

    @Override
    public Page<ChallengeSummaryResponse> getInnovationPipeline(UUID domainId, Pageable pageable) {
        Specification<Challenge> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(root.get("status").in(
                    ChallengeStatus.INNOVATION_REQUIRED,
                    ChallengeStatus.OPEN_FOR_ACADEMIC_PROPOSALS,
                    ChallengeStatus.SOLUTION_PROTOTYPING,
                    ChallengeStatus.FIELD_PILOT_TESTING
            ));

            if (domainId != null) {
                predicates.add(cb.equal(root.get("domain").get("id"), domainId));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return challengeRepository.findAll(spec, pageable).map(ChallengeSummaryResponse::fromEntity);
    }

    @Override
    public List<UniversityChallengeMatchResponse> getMatchingChallengesForUniversity(UUID universityOrgId) {
        Organization univ = organizationRepository.findById(universityOrgId)
                .orElseThrow(() -> new ResourceNotFoundException("University", "id", universityOrgId));

        // Get university domains
        List<UUID> domainIds = organizationDomainRepository.findByOrganizationId(universityOrgId).stream()
                .map(od -> od.getDomain().getId())
                .toList();

        if (domainIds.isEmpty()) {
            domainIds = domainRepository.findAll().stream().map(Domain::getId).toList();
        }

        List<Challenge> openChallenges = challengeRepository.findOpenForAcademicMatchingInDomains(domainIds);
        List<FacultyProfile> facultyList = facultyProfileRepository.findByOrganizationId(universityOrgId);
        List<InstitutionalResource> resourceList = institutionalResourceRepository.findByOrganizationId(universityOrgId);

        List<UniversityChallengeMatchResponse> results = new ArrayList<>();

        for (Challenge c : openChallenges) {
            String chText = (c.getTitle() + " " + c.getDescription()).toLowerCase(Locale.ROOT);

            List<String> matchingFaculty = facultyList.stream()
                    .filter(f -> f.getResearchAreas() != null &&
                            Arrays.stream(f.getResearchAreas().toLowerCase(Locale.ROOT).split(","))
                                    .anyMatch(kw -> chText.contains(kw.trim())))
                    .map(f -> f.getUser().getFullName() + " (" + f.getPrimaryDiscipline() + ")")
                    .collect(Collectors.toList());

            List<String> matchingLabs = resourceList.stream()
                    .filter(r -> chText.contains(r.getResourceName().toLowerCase(Locale.ROOT)))
                    .map(InstitutionalResource::getResourceName)
                    .collect(Collectors.toList());

            // Compute match score
            double baseMatch = 50.0;
            if (domainIds.contains(c.getDomain().getId())) baseMatch += 20.0;
            if (!matchingFaculty.isEmpty()) baseMatch += Math.min(20.0, matchingFaculty.size() * 10.0);
            if (!matchingLabs.isEmpty()) baseMatch += 10.0;

            String justification = null;
            if (c.getDepartmentActions() != null) {
                justification = c.getDepartmentActions().stream()
                        .filter(a -> a.getActionType() == DepartmentActionType.ESCALATED_FOR_INNOVATION)
                        .map(ChallengeDepartmentAction::getEscalationJustification)
                        .findFirst()
                        .orElse(null);
            }

            results.add(UniversityChallengeMatchResponse.fromChallenge(
                    c,
                    justification,
                    matchingFaculty,
                    matchingLabs,
                    Math.min(100.0, baseMatch)
            ));
        }

        // Sort by match score descending
        results.sort((a, b) -> Double.compare(b.getMatchScore(), a.getMatchScore()));
        return results;
    }

    @Override
    public List<TimelineEventResponse> getTimeline(UUID challengeId) {
        return timelineEventRepository.findPublicTimelineByChallengeId(challengeId).stream()
                .map(TimelineEventResponse::fromEntity)
                .collect(Collectors.toList());
    }

    private void createTimelineEvent(
            Challenge challenge,
            ChallengeStatus prevStatus,
            ChallengeStatus newStatus,
            User actor,
            String actorRole,
            String eventTitle,
            String eventMessage,
            boolean isPublic
    ) {
        ChallengeTimelineEvent event = ChallengeTimelineEvent.builder()
                .challenge(challenge)
                .previousStatus(prevStatus)
                .newStatus(newStatus)
                .actor(actor)
                .actorRole(actorRole)
                .eventTitle(eventTitle)
                .eventMessage(eventMessage)
                .isPublic(isPublic)
                .createdAt(Instant.now())
                .build();
        timelineEventRepository.save(event);
        challenge.addTimelineEvent(event);
    }

    private void validateOfficerPermission(Challenge challenge, User officer) {
        if (officer.hasRole(RoleName.SUPER_ADMIN) || officer.hasRole(RoleName.GOVERNMENT_ADMIN)) {
            return;
        }

        if (officer.hasRole(RoleName.GOVERNMENT_OFFICIAL)) {
            if (challenge.getAssignedDepartment() != null) {
                UUID deptOrgId = challenge.getAssignedDepartment().getOrganizationId();
                if (organizationMemberRepository.existsByOrganizationIdAndUserId(deptOrgId, officer.getId())) {
                    return;
                }
            }
        }

        throw new ForbiddenException("You are not authorized to perform department actions on this challenge");
    }

    private String generateTrackingNumber() {
        String datePart = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
        int rand = 10000 + RANDOM.nextInt(90000);
        return "SMX-" + datePart + "-" + rand;
    }

    private String getUserPrimaryRole(User user) {
        return user.getRoles().stream()
                .findFirst()
                .map(r -> r.getName().name())
                .orElse("CITIZEN");
    }
}
