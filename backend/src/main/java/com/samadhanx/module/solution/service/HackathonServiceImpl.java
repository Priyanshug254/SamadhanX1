package com.samadhanx.module.solution.service;

import com.samadhanx.common.exception.BadRequestException;
import com.samadhanx.common.exception.ConflictException;
import com.samadhanx.common.exception.ForbiddenException;
import com.samadhanx.common.exception.ResourceNotFoundException;
import com.samadhanx.module.challenge.entity.Challenge;
import com.samadhanx.module.challenge.repository.ChallengeRepository;
import com.samadhanx.module.organization.entity.Domain;
import com.samadhanx.module.organization.entity.Organization;
import com.samadhanx.module.organization.repository.DomainRepository;
import com.samadhanx.module.organization.repository.OrganizationRepository;
import com.samadhanx.module.role.entity.RoleName;
import com.samadhanx.module.solution.dto.CreateHackathonRequest;
import com.samadhanx.module.solution.dto.HackathonResponse;
import com.samadhanx.module.solution.entity.Hackathon;
import com.samadhanx.module.solution.entity.HackathonEvaluator;
import com.samadhanx.module.solution.entity.enums.HackathonStatus;
import com.samadhanx.module.solution.repository.HackathonEvaluatorRepository;
import com.samadhanx.module.solution.repository.HackathonRepository;
import com.samadhanx.module.user.entity.User;
import com.samadhanx.module.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HackathonServiceImpl implements HackathonService {

    private static final Logger log = LoggerFactory.getLogger(HackathonServiceImpl.class);

    private final HackathonRepository hackathonRepository;
    private final HackathonEvaluatorRepository hackathonEvaluatorRepository;
    private final OrganizationRepository organizationRepository;
    private final DomainRepository domainRepository;
    private final ChallengeRepository challengeRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public HackathonResponse createHackathon(CreateHackathonRequest request, UUID creatorUserId) {
        String code = request.getCode().trim().toUpperCase(Locale.ROOT);
        if (hackathonRepository.existsByCode(code)) {
            throw new ConflictException("Hackathon with code '" + code + "' already exists");
        }

        if (request.getSubmissionDeadline().isAfter(request.getEvaluationDeadline())) {
            throw new BadRequestException("Submission deadline must precede evaluation deadline");
        }

        Organization org = organizationRepository.findById(request.getOrganizerOrgId())
                .orElseThrow(() -> new ResourceNotFoundException("Organizer Organization", "id", request.getOrganizerOrgId()));

        User creator = userRepository.findById(creatorUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", creatorUserId));

        Domain domain = null;
        if (request.getDomainId() != null) {
            domain = domainRepository.findById(request.getDomainId()).orElse(null);
        }

        Hackathon hackathon = Hackathon.builder()
                .title(request.getTitle().trim())
                .code(code)
                .description(request.getDescription().trim())
                .bannerUrl(request.getBannerUrl() != null ? request.getBannerUrl().trim() : null)
                .organizerOrganization(org)
                .domain(domain)
                .submissionDeadline(request.getSubmissionDeadline())
                .evaluationDeadline(request.getEvaluationDeadline())
                .status(HackathonStatus.OPEN_FOR_SUBMISSIONS)
                .createdBy(creator)
                .build();

        // Attach challenges
        if (request.getChallengeIds() != null) {
            for (UUID cId : request.getChallengeIds()) {
                Challenge challenge = challengeRepository.findById(cId).orElse(null);
                if (challenge != null) {
                    hackathon.addChallenge(challenge);
                }
            }
        }

        Hackathon saved = hackathonRepository.save(hackathon);

        // Assign initial evaluators
        if (request.getEvaluatorUserIds() != null) {
            for (UUID evalId : request.getEvaluatorUserIds()) {
                User evalUser = userRepository.findById(evalId).orElse(null);
                if (evalUser != null) {
                    HackathonEvaluator evaluator = HackathonEvaluator.builder()
                            .hackathon(saved)
                            .evaluator(evalUser)
                            .specializationDomain(domain != null ? domain.getName() : "General Innovation")
                            .assignedAt(Instant.now())
                            .build();
                    hackathonEvaluatorRepository.save(evaluator);
                    saved.addEvaluator(evaluator);
                }
            }
        }

        log.info("Created hackathon: {} [{}] by user: {}", saved.getTitle(), saved.getCode(), creator.getEmail());
        return HackathonResponse.fromEntity(saved);
    }

    @Override
    public HackathonResponse getHackathonById(UUID hackathonId) {
        Hackathon h = hackathonRepository.findByIdWithDetails(hackathonId)
                .orElseThrow(() -> new ResourceNotFoundException("Hackathon", "id", hackathonId));
        return HackathonResponse.fromEntity(h);
    }

    @Override
    public HackathonResponse getHackathonByCode(String code) {
        Hackathon h = hackathonRepository.findByCode(code.trim().toUpperCase(Locale.ROOT))
                .orElseThrow(() -> new ResourceNotFoundException("Hackathon", "code", code));
        return HackathonResponse.fromEntity(h);
    }

    @Override
    public Page<HackathonResponse> listHackathons(HackathonStatus status, Pageable pageable) {
        if (status != null) {
            return hackathonRepository.findByStatus(status, pageable).map(HackathonResponse::fromEntity);
        }
        return hackathonRepository.findAll(pageable).map(HackathonResponse::fromEntity);
    }

    @Override
    @Transactional
    public void assignEvaluatorToHackathon(UUID hackathonId, UUID evaluatorUserId, String domain, UUID actionByUserId) {
        Hackathon hackathon = hackathonRepository.findById(hackathonId)
                .orElseThrow(() -> new ResourceNotFoundException("Hackathon", "id", hackathonId));

        User actor = userRepository.findById(actionByUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", actionByUserId));

        if (!actor.hasRole(RoleName.SUPER_ADMIN) && !actor.hasRole(RoleName.GOVERNMENT_ADMIN) && !actor.hasRole(RoleName.UNIVERSITY_ADMIN)) {
            throw new ForbiddenException("Only administrators can assign evaluators to competitions");
        }

        if (hackathonEvaluatorRepository.existsByHackathonIdAndEvaluatorId(hackathonId, evaluatorUserId)) {
            throw new ConflictException("Evaluator is already assigned to this competition");
        }

        User evalUser = userRepository.findById(evaluatorUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", evaluatorUserId));

        HackathonEvaluator evaluator = HackathonEvaluator.builder()
                .hackathon(hackathon)
                .evaluator(evalUser)
                .specializationDomain(domain != null ? domain.trim() : "General Evaluator")
                .assignedAt(Instant.now())
                .build();

        hackathonEvaluatorRepository.save(evaluator);
        log.info("Assigned evaluator {} to hackathon {}", evalUser.getEmail(), hackathon.getCode());
    }
}
