package com.samadhanx.module.partnership.service;

import com.samadhanx.common.exception.BadRequestException;
import com.samadhanx.common.exception.ForbiddenException;
import com.samadhanx.common.exception.ResourceNotFoundException;
import com.samadhanx.module.organization.entity.Organization;
import com.samadhanx.module.organization.entity.enums.VerificationStatus;
import com.samadhanx.module.organization.repository.OrganizationRepository;
import com.samadhanx.module.partnership.dto.PartnerCapabilityRequest;
import com.samadhanx.module.partnership.dto.PartnerCapabilityResponse;
import com.samadhanx.module.partnership.dto.PartnerMatchResponse;
import com.samadhanx.module.partnership.entity.PartnerCapability;
import com.samadhanx.module.partnership.repository.PartnerCapabilityRepository;
import com.samadhanx.module.solution.entity.Proposal;
import com.samadhanx.module.solution.repository.ProposalRepository;
import com.samadhanx.module.user.entity.User;
import com.samadhanx.module.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PartnerMatchingServiceImpl implements PartnerMatchingService {

    private static final Logger log = LoggerFactory.getLogger(PartnerMatchingServiceImpl.class);

    private final PartnerCapabilityRepository partnerCapabilityRepository;
    private final OrganizationRepository organizationRepository;
    private final ProposalRepository proposalRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public PartnerCapabilityResponse registerOrUpdatePartnerCapability(PartnerCapabilityRequest request, UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Organization org = organizationRepository.findById(request.getOrganizationId())
                .orElseThrow(() -> new ResourceNotFoundException("Organization not found"));

        // Only VERIFIED organizations can register partner capabilities
        if (org.getVerificationStatus() != VerificationStatus.VERIFIED) {
            throw new BadRequestException("Only officially VERIFIED organizations can register partner capabilities for ecosystem collaboration");
        }

        PartnerCapability capability = partnerCapabilityRepository.findById(request.getOrganizationId())
                .orElseGet(() -> PartnerCapability.builder()
                        .organizationId(request.getOrganizationId())
                        .organization(org)
                        .createdAt(Instant.now())
                        .build());

        capability.setSectors(request.getSectors());
        capability.setTechnologies(request.getTechnologies());
        capability.setAreasOfInterest(request.getAreasOfInterest());
        capability.setMentoringCapability(request.isMentoringCapability());
        capability.setFundingCapability(request.isFundingCapability());
        capability.setPrototypingCapability(request.isPrototypingCapability());
        capability.setTestingCapability(request.isTestingCapability());
        capability.setDeploymentCapability(request.isDeploymentCapability());
        capability.setGeographicServiceAreas(request.getGeographicServiceAreas());
        capability.setAvailableResourcesBudget(request.getAvailableResourcesBudget() != null ? request.getAvailableResourcesBudget() : BigDecimal.ZERO);
        capability.setUpdatedAt(Instant.now());

        PartnerCapability saved = partnerCapabilityRepository.save(capability);
        log.info("Registered partner capabilities for organization: {} [{}]", org.getName(), org.getCode());
        return PartnerCapabilityResponse.fromEntity(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public PartnerCapabilityResponse getPartnerCapability(UUID organizationId) {
        PartnerCapability capability = partnerCapabilityRepository.findById(organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Partner capability not found for organization"));
        return PartnerCapabilityResponse.fromEntity(capability);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PartnerMatchResponse> findMatchingPartnersForProposal(UUID proposalId) {
        Proposal proposal = proposalRepository.findById(proposalId)
                .orElseThrow(() -> new ResourceNotFoundException("Proposal not found: " + proposalId));

        List<PartnerCapability> capabilities = partnerCapabilityRepository.findVerifiedPartnerCapabilities();
        List<PartnerMatchResponse> matches = new ArrayList<>();

        String challengeDomain = proposal.getChallenge() != null && proposal.getChallenge().getDomain() != null
                ? proposal.getChallenge().getDomain().getName() : "";
        String proposalTitle = proposal.getTitle();
        String proposalTech = proposal.getTechnicalApproach() != null ? proposal.getTechnicalApproach() : "";
        String proposalState = proposal.getChallenge() != null && proposal.getChallenge().getState() != null
                ? proposal.getChallenge().getState() : "";
        String proposalDistrict = proposal.getChallenge() != null && proposal.getChallenge().getDistrict() != null
                ? proposal.getChallenge().getDistrict() : "";

        Set<String> searchKeywords = extractKeywords(challengeDomain + " " + proposalTitle + " " + proposalTech);

        for (PartnerCapability cap : capabilities) {
            Organization org = cap.getOrganization();
            if (org == null) continue;

            double score = 0.0;
            List<String> factors = new ArrayList<>();
            List<String> missing = new ArrayList<>();

            // 1. Sector / Domain Keyword Matching (max 30 pts)
            String orgSectors = (cap.getSectors() != null ? cap.getSectors() : "") + " " +
                                (cap.getAreasOfInterest() != null ? cap.getAreasOfInterest() : "");
            Set<String> orgKeywords = extractKeywords(orgSectors);
            long domainMatches = searchKeywords.stream().filter(orgKeywords::contains).count();
            if (domainMatches > 0) {
                double domainScore = Math.min(30.0, domainMatches * 7.5);
                score += domainScore;
                factors.add(String.format("Sector alignment in '%s' (%d keyword matches, +%.1f pts)", challengeDomain, domainMatches, domainScore));
            } else {
                missing.add("No explicit domain focus overlap");
            }

            // 2. Technology Competency Overlap (max 25 pts)
            String orgTech = cap.getTechnologies() != null ? cap.getTechnologies() : "";
            Set<String> techKeywords = extractKeywords(orgTech);
            long techMatches = searchKeywords.stream().filter(techKeywords::contains).count();
            if (techMatches > 0) {
                double techScore = Math.min(25.0, techMatches * 6.25);
                score += techScore;
                factors.add(String.format("Core technical competency match: %s (+%.1f pts)", orgTech, techScore));
            }

            // 3. Functional Capability Readiness (max 25 pts)
            double capScore = 0.0;
            if (cap.isFundingCapability()) {
                capScore += 7.0;
                factors.add("Offers innovation grants / CSR funding (+7.0 pts)");
            }
            if (cap.isPrototypingCapability()) {
                capScore += 7.0;
                factors.add("Equipped with fabrication / prototyping facilities (+7.0 pts)");
            }
            if (cap.isTestingCapability()) {
                capScore += 6.0;
                factors.add("Equipped with QA and accredited validation labs (+6.0 pts)");
            }
            if (cap.isDeploymentCapability()) {
                capScore += 5.0;
                factors.add("Field distribution & deployment networks (+5.0 pts)");
            }
            score += Math.min(25.0, capScore);

            if (!cap.isPrototypingCapability()) missing.add("Lacks in-house rapid prototyping equipment");
            if (!cap.isTestingCapability()) missing.add("Lacks accredited validation laboratory");

            // 4. Geographic Service Proximity (max 20 pts)
            String geo = cap.getGeographicServiceAreas() != null ? cap.getGeographicServiceAreas().toLowerCase() : "";
            if (StringUtils.hasText(proposalDistrict) && geo.contains(proposalDistrict.toLowerCase())) {
                score += 20.0;
                factors.add(String.format("Local presence in target district '%s' (+20.0 pts)", proposalDistrict));
            } else if (StringUtils.hasText(proposalState) && geo.contains(proposalState.toLowerCase())) {
                score += 15.0;
                factors.add(String.format("Regional coverage in state '%s' (+15.0 pts)", proposalState));
            } else if (geo.contains("all india") || geo.contains("national") || !StringUtils.hasText(geo)) {
                score += 10.0;
                factors.add("National / multi-state service coverage (+10.0 pts)");
            }

            // Normalize score to 100 max
            score = Math.min(100.0, Math.round(score * 100.0) / 100.0);

            String tier;
            if (score >= 80.0) tier = "EXCELLENT";
            else if (score >= 60.0) tier = "GOOD";
            else if (score >= 40.0) tier = "MODERATE";
            else tier = "LOW";

            matches.add(PartnerMatchResponse.builder()
                    .organizationId(org.getId())
                    .organizationName(org.getName())
                    .organizationCode(org.getCode())
                    .organizationType(org.getOrganizationType())
                    .matchScore(score)
                    .matchTier(tier)
                    .matchingFactors(factors)
                    .missingCapabilities(missing)
                    .canMentor(cap.isMentoringCapability())
                    .canFund(cap.isFundingCapability())
                    .canPrototype(cap.isPrototypingCapability())
                    .canTest(cap.isTestingCapability())
                    .canDeploy(cap.isDeploymentCapability())
                    .availableBudget(cap.getAvailableResourcesBudget())
                    .build());
        }

        // Sort descending by match score
        matches.sort(Comparator.comparingDouble(PartnerMatchResponse::getMatchScore).reversed());
        return matches;
    }

    private Set<String> extractKeywords(String text) {
        if (!StringUtils.hasText(text)) return new HashSet<>();
        return Arrays.stream(text.toLowerCase().replaceAll("[^a-zA-Z0-9 ]", " ").split("\\s+"))
                .filter(w -> w.length() > 3)
                .collect(Collectors.toSet());
    }
}
