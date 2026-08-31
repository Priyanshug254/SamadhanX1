package com.samadhanx.module.partnership.service;

import com.samadhanx.common.exception.BadRequestException;
import com.samadhanx.common.exception.ForbiddenException;
import com.samadhanx.common.exception.ResourceNotFoundException;
import com.samadhanx.module.challenge.entity.enums.ChallengeStatus;
import com.samadhanx.module.challenge.repository.ChallengeRepository;
import com.samadhanx.module.organization.entity.Organization;
import com.samadhanx.module.organization.entity.enums.OrganizationType;
import com.samadhanx.module.organization.repository.OrganizationRepository;
import com.samadhanx.module.partnership.dto.*;
import com.samadhanx.module.partnership.entity.*;
import com.samadhanx.module.partnership.entity.enums.KpiName;
import com.samadhanx.module.partnership.entity.enums.PilotStatus;
import com.samadhanx.module.partnership.entity.enums.TestResult;
import com.samadhanx.module.partnership.repository.*;
import com.samadhanx.module.role.entity.RoleName;
import com.samadhanx.module.solution.entity.Proposal;
import com.samadhanx.module.solution.entity.ProposalTimelineEvent;
import com.samadhanx.module.solution.entity.enums.ProposalStatus;
import com.samadhanx.module.solution.repository.ProposalRepository;
import com.samadhanx.module.solution.repository.ProposalTimelineEventRepository;
import com.samadhanx.module.user.entity.User;
import com.samadhanx.module.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PilotDeploymentServiceImpl implements PilotDeploymentService {

    private static final Logger log = LoggerFactory.getLogger(PilotDeploymentServiceImpl.class);

    private final ValidationTestRepository validationTestRepository;
    private final PilotProjectRepository pilotProjectRepository;
    private final ImpactMetricRepository impactMetricRepository;
    private final TechTransferRecordRepository techTransferRepository;
    private final ProposalRepository proposalRepository;
    private final OrganizationRepository organizationRepository;
    private final UserRepository userRepository;
    private final ChallengeRepository challengeRepository;
    private final ProposalTimelineEventRepository timelineEventRepository;
    private final FundingOfferRepository fundingOfferRepository;
    private final CoDevelopmentProjectRepository coDevProjectRepository;
    private final CollaborationRequestRepository collaborationRequestRepository;
    private final MentorshipEngagementRepository mentorshipRepository;

    // ── Validation Testing ───────────────────────────────────────
    @Override
    @Transactional
    public ValidationTestResponse submitValidationTest(SubmitValidationTestRequest request, UUID userId) {
        User user = getUser(userId);
        Proposal proposal = getProposal(request.getProposalId());

        ValidationTest vt = ValidationTest.builder()
                .proposal(proposal)
                .testType(request.getTestType())
                .testEnvironment(request.getTestEnvironment())
                .testDate(Instant.now())
                .evaluatorName(request.getEvaluatorName())
                .parametersTested(request.getParametersTested())
                .testResult(request.getTestResult())
                .issuesIdentified(request.getIssuesIdentified())
                .correctiveActions(request.getCorrectiveActions())
                .evidenceDocumentUrl(request.getEvidenceDocumentUrl())
                .validationRemarks(request.getValidationRemarks())
                .createdBy(user)
                .createdAt(Instant.now())
                .build();

        ValidationTest saved = validationTestRepository.save(vt);
        recordTimeline(proposal, "Validation Test: " + request.getTestResult().name(),
                String.format("%s test in '%s' by %s. Result: %s", request.getTestType(), request.getTestEnvironment(), request.getEvaluatorName(), request.getTestResult()), user);

        log.info("Recorded validation test {} for proposal {}", request.getTestType(), proposal.getTrackingNumber());
        return ValidationTestResponse.fromEntity(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ValidationTestResponse> getValidationTestsForProposal(UUID proposalId) {
        return validationTestRepository.findByProposalId(proposalId).stream()
                .map(ValidationTestResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasPassedValidation(UUID proposalId) {
        return validationTestRepository.existsByProposalIdAndTestResult(proposalId, TestResult.PASSED);
    }

    // ── Pilot Projects ───────────────────────────────────────────
    @Override
    @Transactional
    public PilotProjectResponse createPilotProject(CreatePilotProjectRequest request, UUID userId) {
        User user = getUser(userId);
        Proposal proposal = getProposal(request.getProposalId());

        Organization partner = null;
        if (request.getImplementationPartnerId() != null) {
            partner = organizationRepository.findById(request.getImplementationPartnerId()).orElse(null);
        }

        String pilotCode = generatePilotCode();

        PilotProject pilot = PilotProject.builder()
                .proposal(proposal)
                .pilotCode(pilotCode)
                .locationName(request.getLocationName())
                .district(request.getDistrict())
                .state(request.getState())
                .pincode(request.getPincode())
                .targetPopulation(request.getTargetPopulation())
                .implementationPartner(partner)
                .startDate(Instant.now())
                .expectedEndDate(request.getExpectedEndDate())
                .status(PilotStatus.PLANNED)
                .objectives(request.getObjectives())
                .createdBy(user)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        PilotProject saved = pilotProjectRepository.save(pilot);
        recordTimeline(proposal, "Pilot Project Planned: " + pilotCode,
                String.format("Pilot established at %s, %s (Target: %d beneficiaries).", request.getLocationName(), request.getDistrict(), request.getTargetPopulation()), user);

        log.info("Created pilot project {} for proposal {}", pilotCode, proposal.getTrackingNumber());
        return PilotProjectResponse.fromEntity(saved);
    }

    @Override
    @Transactional
    public PilotProjectResponse updatePilotStatus(UUID pilotId, UpdatePilotStatusRequest request, UUID userId) {
        User user = getUser(userId);
        PilotProject pilot = pilotProjectRepository.findById(pilotId)
                .orElseThrow(() -> new ResourceNotFoundException("Pilot project not found: " + pilotId));

        pilot.setStatus(request.getStatus());
        if (request.getCommunityValidationStatus() != null) {
            pilot.setCommunityValidationStatus(request.getCommunityValidationStatus());
        }
        if (request.getFeedbackNotes() != null) {
            pilot.setFeedbackNotes(request.getFeedbackNotes());
        }
        if (request.getActualEndDate() != null) {
            pilot.setActualEndDate(request.getActualEndDate());
        }
        pilot.setUpdatedAt(Instant.now());

        PilotProject saved = pilotProjectRepository.save(pilot);
        recordTimeline(pilot.getProposal(), "Pilot Status: " + request.getStatus().name(),
                String.format("Pilot %s is now %s. Community validation: %s", pilot.getPilotCode(), request.getStatus(), pilot.getCommunityValidationStatus()), user);

        return PilotProjectResponse.fromEntity(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public PilotProjectResponse getPilotById(UUID pilotId) {
        PilotProject pilot = pilotProjectRepository.findById(pilotId)
                .orElseThrow(() -> new ResourceNotFoundException("Pilot project not found: " + pilotId));
        return PilotProjectResponse.fromEntity(pilot);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PilotProjectResponse> getPilotsForProposal(UUID proposalId) {
        return pilotProjectRepository.findByProposalId(proposalId).stream()
                .map(PilotProjectResponse::fromEntity)
                .collect(Collectors.toList());
    }

    // ── Impact Measurement ───────────────────────────────────────
    @Override
    @Transactional
    public ImpactMetricResponse recordImpactMetric(RecordImpactMetricRequest request, UUID userId) {
        User user = getUser(userId);
        Proposal proposal = getProposal(request.getProposalId());

        PilotProject pilot = null;
        if (request.getPilotId() != null) {
            pilot = pilotProjectRepository.findById(request.getPilotId()).orElse(null);
        }

        ImpactMetric metric = ImpactMetric.builder()
                .pilot(pilot)
                .proposal(proposal)
                .kpiName(request.getKpiName())
                .baselineValue(request.getBaselineValue())
                .targetValue(request.getTargetValue())
                .actualValue(request.getActualValue())
                .unitOfMeasure(request.getUnitOfMeasure())
                .measurementDate(Instant.now())
                .evidenceUrl(request.getEvidenceUrl())
                .remarks(request.getRemarks())
                .createdAt(Instant.now())
                .build();

        ImpactMetric saved = impactMetricRepository.save(metric);
        recordTimeline(proposal, "Impact Metric Recorded: " + request.getKpiName().name(),
                String.format("Measured %s %s (Target: %s, Baseline: %s)", request.getActualValue(), request.getUnitOfMeasure(), request.getTargetValue(), request.getBaselineValue()), user);

        return ImpactMetricResponse.fromEntity(saved);
    }

    @Override
    @Transactional
    public ImpactMetricResponse verifyImpactMetric(UUID metricId, VerifyImpactMetricRequest request, UUID userId) {
        User user = getUser(userId);
        if (!user.hasRole(RoleName.SUPER_ADMIN) &&
            !user.hasRole(RoleName.GOVERNMENT_ADMIN) &&
            !user.hasRole(RoleName.GOVERNMENT_OFFICIAL)) {
            throw new ForbiddenException("Only authorized government officials and platform admins can verify impact metrics");
        }

        ImpactMetric metric = impactMetricRepository.findById(metricId)
                .orElseThrow(() -> new ResourceNotFoundException("Impact metric not found: " + metricId));

        metric.setVerificationStatus(request.getVerificationStatus());
        metric.setVerifiedByUser(user);
        metric.setVerifiedAt(Instant.now());
        if (request.getRemarks() != null) {
            metric.setRemarks(request.getRemarks());
        }

        ImpactMetric saved = impactMetricRepository.save(metric);
        recordTimeline(metric.getProposal(), "Impact Verified by Government",
                String.format("KPI %s verified: %s by %s", metric.getKpiName(), request.getVerificationStatus(), user.getFullName()), user);

        return ImpactMetricResponse.fromEntity(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ImpactMetricResponse> getImpactMetricsForProposal(UUID proposalId) {
        return impactMetricRepository.findByProposalId(proposalId).stream()
                .map(ImpactMetricResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ProjectImpactSummaryResponse getProjectImpactSummary(UUID proposalId) {
        Proposal proposal = getProposal(proposalId);
        List<PilotProject> pilots = pilotProjectRepository.findByProposalId(proposalId);
        List<ImpactMetric> metrics = impactMetricRepository.findByProposalId(proposalId);

        int totalPopulation = pilots.stream().mapToInt(p -> p.getTargetPopulation() != null ? p.getTargetPopulation() : 0).sum();
        long verified = metrics.stream().filter(m -> "VERIFIED_BY_GOVERNMENT".equals(m.getVerificationStatus().name())).count();

        return ProjectImpactSummaryResponse.builder()
                .proposalId(proposal.getId())
                .proposalTrackingNumber(proposal.getTrackingNumber())
                .proposalTitle(proposal.getTitle())
                .totalPilotsCount(pilots.size())
                .totalTargetPopulation(totalPopulation)
                .verifiedMetricsCount((int) verified)
                .reportedMetricsCount(metrics.size())
                .metrics(metrics.stream().map(ImpactMetricResponse::fromEntity).collect(Collectors.toList()))
                .build();
    }

    // ── Technology Transfer ──────────────────────────────────────
    @Override
    @Transactional
    public TechTransferResponse recordTechTransfer(RecordTechTransferRequest request, UUID userId) {
        User user = getUser(userId);
        Proposal proposal = getProposal(request.getProposalId());
        Organization receivingOrg = organizationRepository.findById(request.getReceivingOrganizationId())
                .orElseThrow(() -> new ResourceNotFoundException("Receiving organization not found"));

        TechTransferRecord ttr = TechTransferRecord.builder()
                .proposal(proposal)
                .assetName(request.getAssetName())
                .ipRegistrationNumber(request.getIpRegistrationNumber())
                .licensingType(request.getLicensingType())
                .receivingOrganization(receivingOrg)
                .transferDate(Instant.now())
                .responsibleParties(request.getResponsibleParties())
                .deploymentStatus(request.getDeploymentStatus())
                .documentationUrl(request.getDocumentationUrl())
                .createdBy(user)
                .createdAt(Instant.now())
                .build();

        TechTransferRecord saved = techTransferRepository.save(ttr);
        recordTimeline(proposal, "Technology Transferred: " + receivingOrg.getName(),
                String.format("Licensed %s under %s terms to %s.", request.getAssetName(), request.getLicensingType(), receivingOrg.getName()), user);

        log.info("Recorded technology transfer for proposal {} to org {}", proposal.getTrackingNumber(), receivingOrg.getName());
        return TechTransferResponse.fromEntity(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TechTransferResponse> getTechTransfersForProposal(UUID proposalId) {
        return techTransferRepository.findByProposalId(proposalId).stream()
                .map(TechTransferResponse::fromEntity)
                .collect(Collectors.toList());
    }

    // ── Government Oversight Dashboard ───────────────────────────
    @Override
    @Transactional(readOnly = true)
    public GovernmentOversightDashboardResponse getGovernmentOversightDashboard() {
        long totalChallenges = challengeRepository.count();
        long openChallenges = challengeRepository.countByStatus(ChallengeStatus.OPEN_FOR_ACADEMIC_PROPOSALS);
        long innovationReqChallenges = challengeRepository.countByStatus(ChallengeStatus.INNOVATION_REQUIRED);
        long resolvedChallenges = challengeRepository.countByStatus(ChallengeStatus.RESOLVED_BY_DEPARTMENT);

        long participatingUnivs = organizationRepository.countByOrganizationType(OrganizationType.UNIVERSITY);
        long verifiedIndustries = organizationRepository.countByOrganizationType(OrganizationType.INDUSTRY);
        long verifiedStartups = organizationRepository.countByOrganizationType(OrganizationType.STARTUP);
        long verifiedMsmes = organizationRepository.countByOrganizationType(OrganizationType.MSME);
        long verifiedCsr = organizationRepository.countByOrganizationType(OrganizationType.CSR);
        long verifiedResearchLabs = organizationRepository.countByOrganizationType(OrganizationType.RESEARCH_LAB);
        long verifiedInnovationHubs = organizationRepository.countByOrganizationType(OrganizationType.INNOVATION_HUB);

        long totalProposals = proposalRepository.count();
        long shortlisted = proposalRepository.countByStatus(ProposalStatus.SHORTLISTED);
        long prototyping = proposalRepository.countByStatus(ProposalStatus.PROTOTYPING);
        long testing = proposalRepository.countByStatus(ProposalStatus.TESTING);
        long pilotReady = proposalRepository.countByStatus(ProposalStatus.PILOT_READY);
        long deployed = proposalRepository.countByStatus(ProposalStatus.DEPLOYED);

        long activePilots = pilotProjectRepository.countByStatus(PilotStatus.ACTIVE);
        long completedPilots = pilotProjectRepository.countByStatus(PilotStatus.COMPLETED);
        long totalPopulation = pilotProjectRepository.sumTotalTargetPopulation();
        long districtsCovered = pilotProjectRepository.countDistinctDistrictsCovered();

        BigDecimal approvedFunding = fundingOfferRepository.sumTotalApprovedFunding();
        BigDecimal waterSaved = impactMetricRepository.sumActualValueByKpiName(KpiName.WATER_SAVED_LITERS_PER_DAY);
        BigDecimal energySaved = impactMetricRepository.sumActualValueByKpiName(KpiName.ENERGY_SAVED_KWH);

        long activeCollabs = collaborationRequestRepository.count();
        long activeMentorships = mentorshipRepository.count();
        long activeCoDevs = coDevProjectRepository.count();
        long techTransfers = techTransferRepository.count();

        return GovernmentOversightDashboardResponse.builder()
                .totalChallenges(totalChallenges)
                .openChallenges(openChallenges)
                .departmentalResolvedChallenges(resolvedChallenges)
                .innovationRequiredChallenges(innovationReqChallenges)
                .participatingUniversities(participatingUnivs)
                .activeAcademicProjects(totalProposals)
                .verifiedIndustries(verifiedIndustries)
                .verifiedStartups(verifiedStartups)
                .verifiedMsmes(verifiedMsmes)
                .verifiedCsrEntities(verifiedCsr)
                .verifiedResearchLabs(verifiedResearchLabs)
                .verifiedInnovationHubs(verifiedInnovationHubs)
                .activeCollaborations(activeCollabs)
                .activeMentorshipEngagements(activeMentorships)
                .totalApprovedFundingInr(approvedFunding != null ? approvedFunding : BigDecimal.ZERO)
                .activeCoDevProjects(activeCoDevs)
                .proposalsSubmitted(totalProposals)
                .proposalsShortlisted(shortlisted)
                .proposalsPrototyping(prototyping)
                .proposalsTesting(testing)
                .proposalsPilotReady(pilotReady)
                .activePilotsCount(activePilots)
                .completedPilotsCount(completedPilots)
                .proposalsDeployed(deployed)
                .techTransfersCount(techTransfers)
                .totalPopulationBenefited(totalPopulation)
                .distinctDistrictsCovered(districtsCovered)
                .totalWaterSavedLitersPerDay(waterSaved != null ? waterSaved : BigDecimal.ZERO)
                .totalEnergySavedKwh(energySaved != null ? energySaved : BigDecimal.ZERO)
                .build();
    }

    // ── Helper Methods ────────────────────────────────────────────
    private User getUser(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
    }

    private Proposal getProposal(UUID proposalId) {
        return proposalRepository.findById(proposalId)
                .orElseThrow(() -> new ResourceNotFoundException("Proposal not found: " + proposalId));
    }

    private String generatePilotCode() {
        YearMonth ym = YearMonth.now();
        int randomNum = 10000 + new Random().nextInt(90000);
        return String.format("PLT-%d-%02d-%d", ym.getYear(), ym.getMonthValue(), randomNum);
    }

    private void recordTimeline(Proposal proposal, String title, String message, User actor) {
        ProposalTimelineEvent event = ProposalTimelineEvent.builder()
                .proposal(proposal)
                .previousStatus(proposal.getStatus())
                .newStatus(proposal.getStatus())
                .actor(actor)
                .actorRole(actor.hasRole(RoleName.SUPER_ADMIN) ? "SUPER_ADMIN" :
                           actor.hasRole(RoleName.GOVERNMENT_ADMIN) ? "GOVERNMENT_ADMIN" :
                           actor.hasRole(RoleName.FACULTY) ? "FACULTY" : "PARTNER")
                .eventTitle(title)
                .eventMessage(message)
                .createdAt(Instant.now())
                .build();
        timelineEventRepository.save(event);
    }
}
