package com.samadhanx.module.demo.service;

import com.samadhanx.module.challenge.entity.Challenge;
import com.samadhanx.module.challenge.entity.enums.ChallengeStatus;
import com.samadhanx.module.challenge.entity.enums.ResolutionPath;
import com.samadhanx.module.challenge.entity.enums.SeverityLevel;
import com.samadhanx.module.challenge.entity.enums.UrgencyLevel;
import com.samadhanx.module.challenge.repository.ChallengeRepository;
import com.samadhanx.module.governance.entity.ApprovalRequest;
import com.samadhanx.module.governance.entity.WorkItem;
import com.samadhanx.module.governance.entity.enums.ApprovalStatus;
import com.samadhanx.module.governance.entity.enums.WorkItemPriority;
import com.samadhanx.module.governance.entity.enums.WorkItemStatus;
import com.samadhanx.module.governance.entity.enums.WorkItemType;
import com.samadhanx.module.governance.entity.enums.WorkflowActionType;
import com.samadhanx.module.governance.repository.ApprovalRequestRepository;
import com.samadhanx.module.governance.repository.WorkItemRepository;
import com.samadhanx.module.notification.entity.NotificationRecord;
import com.samadhanx.module.notification.entity.enums.NotificationType;
import com.samadhanx.module.notification.repository.NotificationRecordRepository;
import com.samadhanx.module.organization.entity.Department;
import com.samadhanx.module.organization.entity.Domain;
import com.samadhanx.module.organization.entity.Organization;
import com.samadhanx.module.organization.entity.enums.GovernmentLevel;
import com.samadhanx.module.organization.entity.enums.OrganizationType;
import com.samadhanx.module.organization.entity.enums.VerificationStatus;
import com.samadhanx.module.organization.repository.DepartmentRepository;
import com.samadhanx.module.organization.repository.DomainRepository;
import com.samadhanx.module.organization.repository.OrganizationRepository;
import com.samadhanx.module.partnership.entity.PilotProject;
import com.samadhanx.module.partnership.entity.enums.PilotStatus;
import com.samadhanx.module.partnership.repository.PilotProjectRepository;
import com.samadhanx.module.role.entity.Role;
import com.samadhanx.module.role.entity.RoleName;
import com.samadhanx.module.role.repository.RoleRepository;
import com.samadhanx.module.solution.entity.Proposal;
import com.samadhanx.module.solution.entity.Team;
import com.samadhanx.module.solution.entity.enums.ProposalStatus;
import com.samadhanx.module.solution.entity.enums.TeamStatus;
import com.samadhanx.module.solution.repository.ProposalRepository;
import com.samadhanx.module.solution.repository.TeamRepository;
import com.samadhanx.module.user.entity.User;
import com.samadhanx.module.user.repository.UserRepository;
import com.samadhanx.module.user.repository.UserRoleRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DemoDataSeederServiceImpl implements DemoDataSeederService {

    private static final Logger log = LoggerFactory.getLogger(DemoDataSeederServiceImpl.class);

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final DomainRepository domainRepository;
    private final OrganizationRepository organizationRepository;
    private final DepartmentRepository departmentRepository;
    private final ChallengeRepository challengeRepository;
    private final TeamRepository teamRepository;
    private final ProposalRepository proposalRepository;
    private final PilotProjectRepository pilotProjectRepository;
    private final WorkItemRepository workItemRepository;
    private final ApprovalRequestRepository approvalRequestRepository;
    private final NotificationRecordRepository notificationRecordRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public boolean isDemoDataPresent() {
        return userRepository.findByEmailIgnoreCase("citizen@samadhanx.org").isPresent() &&
                challengeRepository.findByTrackingNumber("SMX-2026-08-00101").isPresent();
    }

    @Override
    @Transactional
    public void resetAndSeedCompleteEcosystem() {
        log.info("Starting safe SamadhanX demo data initialization and seeding...");

        String encodedPassword = passwordEncoder.encode("Password@123");

        // 1. Seed Roles if missing
        Map<RoleName, Role> roleMap = new EnumMap<>(RoleName.class);
        for (RoleName rn : RoleName.values()) {
            Role role = roleRepository.findByName(rn).orElseGet(() -> {
                Role newRole = Role.builder().name(rn).description("Role " + rn.name()).build();
                return roleRepository.save(newRole);
            });
            roleMap.put(rn, role);
        }

        // 2. Seed Users
        User citizen = getOrCreateUser("citizen@samadhanx.org", "Rajesh", "Verma", encodedPassword, roleMap.get(RoleName.CITIZEN));
        User official = getOrCreateUser("official@samadhanx.gov.in", "Anita", "Pandey", encodedPassword, roleMap.get(RoleName.GOVERNMENT_OFFICIAL));
        User admin = getOrCreateUser("admin@samadhanx.gov.in", "Vikram", "Shukla", encodedPassword, roleMap.get(RoleName.GOVERNMENT_ADMIN));
        User faculty = getOrCreateUser("faculty@iitbhu.ac.in", "Prof. K. R.", "Iyer", encodedPassword, roleMap.get(RoleName.FACULTY));
        User student = getOrCreateUser("student@iitbhu.ac.in", "Rahul", "Mishra", encodedPassword, roleMap.get(RoleName.STUDENT));
        User csrLead = getOrCreateUser("csr@tatatrusts.org", "Meera", "Tata", encodedPassword, roleMap.get(RoleName.CSR));
        User evaluator = getOrCreateUser("evaluator@dst.gov.in", "Dr. A. K.", "Roy", encodedPassword, roleMap.get(RoleName.FACULTY));

        // 3. Seed Domains
        Domain waterDomain = domainRepository.findByCode("WATER_SAN").orElseGet(() ->
                domainRepository.save(Domain.builder()
                        .code("WATER_SAN")
                        .name("Water & Sanitation")
                        .description("Potable water access, arsenic/fluoride filtration, and sewer networks")
                        .active(true)
                        .build())
        );

        Domain energyDomain = domainRepository.findByCode("CLEAN_ENERGY").orElseGet(() ->
                domainRepository.save(Domain.builder()
                        .code("CLEAN_ENERGY")
                        .name("Clean Energy & Agri-Power")
                        .description("Decentralized solar microgrids, cold chain storage, and biomass")
                        .active(true)
                        .build())
        );

        Domain infraDomain = domainRepository.findByCode("URBAN_INFRA").orElseGet(() ->
                domainRepository.save(Domain.builder()
                        .code("URBAN_INFRA")
                        .name("Urban & Rural Infrastructure")
                        .description("Road subsidence, drainage overcapacity, and bridge safety")
                        .active(true)
                        .build())
        );

        // 4. Seed Organizations & Departments
        Organization iitBhu = organizationRepository.findByCodeIgnoreCase("IIT-BHU-VARANASI").orElseGet(() ->
                organizationRepository.save(Organization.builder()
                        .id(UUID.randomUUID())
                        .name("Indian Institute of Technology (BHU) Varanasi")
                        .code("IIT-BHU-VARANASI")
                        .organizationType(OrganizationType.UNIVERSITY)
                        .contactEmail("contact@iitbhu.ac.in")
                        .district("Varanasi")
                        .state("Uttar Pradesh")
                        .verificationStatus(VerificationStatus.VERIFIED)
                        .build())
        );

        Organization pwdOrg = organizationRepository.findByCodeIgnoreCase("UP-PWD-VARANASI").orElseGet(() ->
                organizationRepository.save(Organization.builder()
                        .id(UUID.randomUUID())
                        .name("Public Works Department (Varanasi Division)")
                        .code("UP-PWD-VARANASI")
                        .organizationType(OrganizationType.GOVERNMENT_BODY)
                        .contactEmail("pwd.varanasi@up.gov.in")
                        .district("Varanasi")
                        .state("Uttar Pradesh")
                        .verificationStatus(VerificationStatus.VERIFIED)
                        .build())
        );

        Department pwdDept = departmentRepository.findById(pwdOrg.getId()).orElseGet(() ->
                departmentRepository.save(Department.builder()
                        .organizationId(pwdOrg.getId())
                        .organization(pwdOrg)
                        .level(GovernmentLevel.DISTRICT)
                        .jurisdictionArea("Varanasi Division")
                        .nodalOfficerName("Anita Pandey")
                        .nodalOfficerEmail("official@samadhanx.gov.in")
                        .build())
        );

        Organization tataTrusts = organizationRepository.findByCodeIgnoreCase("TATA-TRUSTS-CSR").orElseGet(() ->
                organizationRepository.save(Organization.builder()
                        .id(UUID.randomUUID())
                        .name("Tata Trusts Social Development Foundation")
                        .code("TATA-TRUSTS-CSR")
                        .organizationType(OrganizationType.CSR)
                        .contactEmail("csr@tatatrusts.org")
                        .district("Mumbai")
                        .state("Maharashtra")
                        .verificationStatus(VerificationStatus.VERIFIED)
                        .build())
        );

        // 5. Seed Challenges
        // Challenge 1: Arsenic contamination (INNOVATION_REQUIRED)
        Challenge c1 = challengeRepository.findByTrackingNumber("SMX-2026-08-00101").orElseGet(() -> {
            Challenge c = Challenge.builder()
                    .id(UUID.randomUUID())
                    .trackingNumber("SMX-2026-08-00101")
                    .title("Severe Fluoride and Arsenic Contamination in Rural Hand Pumps")
                    .description("Village borewells in Chandauli show 4.8 mg/L fluoride and trace arsenic levels, exceeding BIS safe limits. Over 1,200 villagers exhibiting early dental and skeletal fluorosis symptoms. Standard bleaching media saturates in 3 weeks.")
                    .domain(waterDomain)
                    .submittedBy(citizen)
                    .submitterType(com.samadhanx.module.challenge.entity.enums.SubmitterType.CITIZEN)
                    .latitude(new BigDecimal("25.2612"))
                    .longitude(new BigDecimal("83.2644"))
                    .district("Chandauli")
                    .state("Uttar Pradesh")
                    .pincode("232104")
                    .jurisdictionLevel(GovernmentLevel.DISTRICT)
                    .severityLevel(SeverityLevel.CRITICAL)
                    .urgencyLevel(UrgencyLevel.IMMEDIATE)
                    .estimatedAffectedPopulation(2400)
                    .status(ChallengeStatus.INNOVATION_REQUIRED)
                    .resolutionPath(ResolutionPath.INNOVATION_RESEARCH)
                    .priorityScore(new BigDecimal("94.50"))
                    .aiConfidenceScore(new BigDecimal("0.96"))
                    .aiReasoning("High geographic co-occurrence of toxic non-degradable chemical contaminants (Fluoride/Arsenic). Standard filtration insufficient without recurring media replacement.")
                    .aiPriorityReasoning("Priority boosted (+25) due to severe health hazard, population vulnerability index, and geo-spatial cluster.")
                    .aiDuplicateExplanation("Nearest comparable challenge SMX-2026-08-00104 is 18.4km away with different chemical profile.")
                    .aiModelProvider("Google Gemini 1.5-Flash (Deterministic Fail-Safe Active)")
                    .assignedDepartment(pwdDept)
                    .build();
            return challengeRepository.save(c);
        });

        // Challenge 2: GT Road Subsidence (RESOLVED_BY_DEPARTMENT)
        Challenge c2 = challengeRepository.findByTrackingNumber("SMX-2026-08-00102").orElseGet(() -> {
            Challenge c = Challenge.builder()
                    .id(UUID.randomUUID())
                    .trackingNumber("SMX-2026-08-00102")
                    .title("Structural Pavement Subsidence and Silt Blockage on GT Road Bypass")
                    .description("Monsoon culvert overflow caused 1.5m tarmac depression near Lanka intersection, creating major traffic choke points and water logging.")
                    .domain(infraDomain)
                    .submittedBy(citizen)
                    .submitterType(com.samadhanx.module.challenge.entity.enums.SubmitterType.CITIZEN)
                    .latitude(new BigDecimal("25.2815"))
                    .longitude(new BigDecimal("82.9992"))
                    .district("Varanasi")
                    .state("Uttar Pradesh")
                    .pincode("221005")
                    .jurisdictionLevel(GovernmentLevel.DISTRICT)
                    .severityLevel(SeverityLevel.HIGH)
                    .urgencyLevel(UrgencyLevel.HIGH)
                    .estimatedAffectedPopulation(8500)
                    .status(ChallengeStatus.RESOLVED_BY_DEPARTMENT)
                    .resolutionPath(ResolutionPath.DEPARTMENTAL_STANDARD)
                    .priorityScore(new BigDecimal("74.20"))
                    .aiConfidenceScore(new BigDecimal("0.92"))
                    .aiReasoning("Civil engineering structural blockage resolvable via standard asphalt reinforcement and culvert de-silting.")
                    .assignedDepartment(pwdDept)
                    .build();
            return challengeRepository.save(c);
        });

        // 6. Seed Innovation Team & Proposal
        Team team = teamRepository.findByChallengeId(c1.getId()).stream().findFirst().orElseGet(() -> {
            Team t = Team.builder()
                    .id(UUID.randomUUID())
                    .teamName("JalShuddhi Terracotta Innovation Lab")
                    .description("Multidisciplinary research consortium between Ceramic Engineering, Environmental Chemistry, and IoT Systems.")
                    .challenge(c1)
                    .homeUniversity(iitBhu)
                    .createdBy(student)
                    .status(TeamStatus.ACTIVE)
                    .build();
            return teamRepository.save(t);
        });

        Proposal proposal = proposalRepository.findByTrackingNumber("PRP-2026-08-001").orElseGet(() -> {
            Proposal p = Proposal.builder()
                    .id(UUID.randomUUID())
                    .trackingNumber("PRP-2026-08-001")
                    .challenge(c1)
                    .team(team)
                    .submittedBy(student)
                    .title("Gravity-Fed Terracotta Hydroxyapatite Nanocomposite Filter with IoT Telemetry")
                    .problemUnderstanding("Continuous gravity-flow adsorption using locally sourced clay enriched with synthetic hydroxyapatite removes 99.2% fluoride and arsenic ions without electric power.")
                    .proposedSolution("Low-cost modular ceramic cartridges fitted with solar ESP32 IoT water quality sensor measuring TDS, pH, and flow velocity.")
                    .innovationNovelty("Dual-stage calcined hydroxyapatite matrix regenerable via mild vinegar/brine solution.")
                    .technicalApproach("Stage 1: Micro-porous terracotta ceramic pre-filter. Stage 2: Hydroxyapatite-coated activated alumina core. Stage 3: Low-power NB-IoT sensor node.")
                    .expectedImpact("Provides WHO-compliant drinking water to 2,400 villagers at ₹0.04 per liter.")
                    .implementationPlan("Phase 1: Lab prototype (TRL 3). Phase 2: Community pilot in Chandauli (TRL 6). Phase 3: District roll-out (TRL 8).")
                    .estimatedCostInr(new BigDecimal("485000.00"))
                    .status(ProposalStatus.PILOT_READY)
                    .shortlisted(true)
                    .averageScore(new BigDecimal("92.40"))
                    .evaluationCount(3)
                    .build();
            return proposalRepository.save(p);
        });

        // 7. Seed Pilot Project
        PilotProject pilot = pilotProjectRepository.findByProposalId(proposal.getId()).stream().findFirst().orElseGet(() -> {
            PilotProject pl = PilotProject.builder()
                    .id(UUID.randomUUID())
                    .pilotCode("PLT-2026-001")
                    .locationName("Chandauli Community Potable Water Pilot Testbed")
                    .proposal(proposal)
                    .implementationPartner(tataTrusts)
                    .createdBy(csrLead)
                    .district("Chandauli")
                    .state("Uttar Pradesh")
                    .targetPopulation(1850)
                    .status(PilotStatus.ACTIVE)
                    .objectives("Field installation of 10 community filtration units across 4 handpump sites in Chandauli district with continuous IoT monitoring.")
                    .startDate(Instant.now().minus(14, ChronoUnit.DAYS))
                    .expectedEndDate(Instant.now().plus(90, ChronoUnit.DAYS))
                    .build();
            return pilotProjectRepository.save(pl);
        });

        // 8. Seed Work Items & Approvals
        if (workItemRepository.findByChallengeId(c1.getId()).isEmpty()) {
            workItemRepository.save(WorkItem.builder()
                    .title("Validate Chandauli Well Fluoride Field Telemetry")
                    .description("Review IoT turbidity and sensor telemetry baseline for Community Pilot 01.")
                    .itemType(WorkItemType.PILOT_DEPLOYMENT)
                    .status(WorkItemStatus.IN_PROGRESS)
                    .priority(WorkItemPriority.HIGH)
                    .assignedTo(official)
                    .creatorUser(admin)
                    .challengeId(c1.getId())
                    .challengeTrackingNumber(c1.getTrackingNumber())
                    .proposalId(proposal.getId())
                    .proposalTrackingNumber(proposal.getTrackingNumber())
                    .dueDate(Instant.now().plus(3, ChronoUnit.DAYS))
                    .build());

            workItemRepository.save(WorkItem.builder()
                    .title("Review Milestone 2 Filter Media Regeneration Protocol")
                    .description("Evaluate university proposal laboratory test logs for TRL 6 sign-off.")
                    .itemType(WorkItemType.PROPOSAL_REVIEW)
                    .status(WorkItemStatus.TODO)
                    .priority(WorkItemPriority.CRITICAL)
                    .assignedTo(evaluator)
                    .creatorUser(admin)
                    .proposalId(proposal.getId())
                    .proposalTrackingNumber(proposal.getTrackingNumber())
                    .dueDate(Instant.now().plus(1, ChronoUnit.DAYS))
                    .build());
        }

        if (approvalRequestRepository.findByTargetEntityId(c1.getId()).isEmpty()) {
            approvalRequestRepository.save(ApprovalRequest.builder()
                    .workflowType(WorkflowActionType.CHALLENGE_ESCALATION)
                    .targetEntityId(c1.getId())
                    .targetReferenceCode(c1.getTrackingNumber())
                    .requestedBy(official)
                    .reviewedBy(admin)
                    .status(ApprovalStatus.APPROVED)
                    .justification("Fluoride concentration exceeds statutory departmental budget threshold; requires academic R&D.")
                    .reviewComments("Approved for National University Innovation Pipeline.")
                    .reviewedAt(Instant.now().minus(2, ChronoUnit.DAYS))
                    .build());
        }

        // 9. Seed Representative Notifications
        if (notificationRecordRepository.countByUserIdAndIsReadFalse(citizen.getId()) == 0) {
            notificationRecordRepository.save(NotificationRecord.builder()
                    .user(citizen)
                    .title("Challenge Escalated to University Innovation Hub")
                    .body("Your challenge SMX-2026-08-00101 has been escalated to academic researchers at IIT BHU for advanced ceramic filter R&D.")
                    .notificationType(NotificationType.INNOVATION_REQUIRED)
                    .referenceId(c1.getId().toString())
                    .referenceType("CHALLENGE")
                    .isRead(false)
                    .build());

            notificationRecordRepository.save(NotificationRecord.builder()
                    .user(official)
                    .title("New Critical Work Item Assigned")
                    .body("Task: Validate Chandauli Well Fluoride Field Telemetry assigned by Admin.")
                    .notificationType(NotificationType.GENERAL)
                    .referenceId(c1.getId().toString())
                    .referenceType("WORK_ITEM")
                    .isRead(false)
                    .build());
        }

        log.info("Safe SamadhanX demo data seeded successfully with realistic Varanasi/Chandauli testbeds!");
    }

    private User getOrCreateUser(String email, String firstName, String lastName, String passwordHash, Role role) {
        return userRepository.findByEmailIgnoreCase(email).orElseGet(() -> {
            User u = User.builder()
                    .email(email)
                    .firstName(firstName)
                    .lastName(lastName)
                    .passwordHash(passwordHash)
                    .isActive(true)
                    .build();
            if (role != null) {
                u.addRole(role);
            }
            return userRepository.save(u);
        });
    }
}
