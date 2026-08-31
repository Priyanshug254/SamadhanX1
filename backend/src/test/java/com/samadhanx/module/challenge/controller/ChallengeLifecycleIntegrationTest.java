package com.samadhanx.module.challenge.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.samadhanx.module.auth.dto.LoginRequest;
import com.samadhanx.module.auth.dto.RegisterRequest;
import com.samadhanx.module.challenge.dto.AttachmentRequest;
import com.samadhanx.module.challenge.dto.DepartmentActionRequest;
import com.samadhanx.module.challenge.dto.DepartmentResolveRequest;
import com.samadhanx.module.challenge.dto.EndorsementRequest;
import com.samadhanx.module.challenge.dto.EscalateToInnovationRequest;
import com.samadhanx.module.challenge.dto.SubmitChallengeRequest;
import com.samadhanx.module.challenge.entity.enums.DepartmentActionType;
import com.samadhanx.module.challenge.entity.enums.MediaType;
import com.samadhanx.module.challenge.entity.enums.SeverityLevel;
import com.samadhanx.module.challenge.entity.enums.SubmitterType;
import com.samadhanx.module.challenge.entity.enums.UrgencyLevel;
import com.samadhanx.module.organization.dto.FacultyProfileRequest;
import com.samadhanx.module.organization.dto.InstitutionalResourceRequest;
import com.samadhanx.module.organization.dto.RegisterOrganizationRequest;
import com.samadhanx.module.organization.dto.ReviewVerificationRequest;
import com.samadhanx.module.organization.dto.SubmitVerificationRequest;
import com.samadhanx.module.organization.dto.SupportingDocumentRequest;
import com.samadhanx.module.organization.dto.UniversityProfileRequest;
import com.samadhanx.module.organization.entity.enums.DocumentType;
import com.samadhanx.module.organization.entity.enums.GovernmentLevel;
import com.samadhanx.module.organization.entity.enums.InstitutionType;
import com.samadhanx.module.organization.entity.enums.OrganizationType;
import com.samadhanx.module.organization.entity.enums.ResourceType;
import com.samadhanx.module.organization.entity.enums.VerificationStatus;
import com.samadhanx.module.role.entity.Role;
import com.samadhanx.module.role.entity.RoleName;
import com.samadhanx.module.role.repository.RoleRepository;
import com.samadhanx.module.user.entity.User;
import com.samadhanx.module.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Challenge Lifecycle & Academic Innovation Ecosystem Integration Tests")
class ChallengeLifecycleIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String adminToken;
    private String citizenToken;
    private String facultyToken;
    private String univOrgId;

    @BeforeEach
    void setUp() throws Exception {
        long ts = System.currentTimeMillis();

        // 1. Super Admin
        String adminEmail = "admin.m3." + ts + "@samadhanx.gov.in";
        Role superAdminRole = roleRepository.findByName(RoleName.SUPER_ADMIN).orElseGet(() ->
                roleRepository.save(Role.builder().name(RoleName.SUPER_ADMIN).description("Super Admin").build())
        );

        User adminUser = User.builder()
                .email(adminEmail)
                .passwordHash(passwordEncoder.encode("AdminPass@123"))
                .firstName("Platform")
                .lastName("Admin")
                .isActive(true)
                .build();
        adminUser.addRole(superAdminRole);
        userRepository.save(adminUser);

        MvcResult adminLogin = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(LoginRequest.builder().email(adminEmail).password("AdminPass@123").build())))
                .andExpect(status().isOk())
                .andReturn();
        adminToken = objectMapper.readTree(adminLogin.getResponse().getContentAsString()).get("data").get("accessToken").asText();

        // 2. Citizen Submitter
        String citizenEmail = "citizen.m3." + ts + "@samadhanx.org";
        RegisterRequest citizenReq = RegisterRequest.builder()
                .email(citizenEmail)
                .password("CitizenPass@123")
                .firstName("Sunita")
                .lastName("Devi")
                .role(RoleName.CITIZEN)
                .build();
        MvcResult citizenReg = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(citizenReq)))
                .andExpect(status().isCreated())
                .andReturn();
        citizenToken = objectMapper.readTree(citizenReg.getResponse().getContentAsString()).get("data").get("accessToken").asText();

        // 3. Faculty Expert & University Onboarding
        String facultyEmail = "prof.sharma." + ts + "@bhu.ac.in";
        RegisterRequest facultyReq = RegisterRequest.builder()
                .email(facultyEmail)
                .password("FacultyPass@123")
                .firstName("Prof. Rajesh")
                .lastName("Sharma")
                .role(RoleName.FACULTY)
                .build();
        MvcResult facultyReg = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(facultyReq)))
                .andExpect(status().isCreated())
                .andReturn();
        facultyToken = objectMapper.readTree(facultyReg.getResponse().getContentAsString()).get("data").get("accessToken").asText();

        // Register & Verify University (IIT/BHU)
        String univCode = "BHU-TECH-" + ts;
        RegisterOrganizationRequest univOrgReq = RegisterOrganizationRequest.builder()
                .name("Banaras Hindu University Engineering")
                .code(univCode)
                .organizationType(OrganizationType.UNIVERSITY)
                .contactEmail("rnd@" + univCode.toLowerCase() + ".ac.in")
                .district("Varanasi")
                .state("Uttar Pradesh")
                .domainCodes(List.of("WATER_SANITATION", "CLEAN_ENERGY"))
                .build();

        MvcResult univOrgRes = mockMvc.perform(post("/api/v1/organizations")
                        .header("Authorization", "Bearer " + facultyToken)
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(univOrgReq)))
                .andExpect(status().isCreated())
                .andReturn();
        univOrgId = objectMapper.readTree(univOrgRes.getResponse().getContentAsString()).get("data").get("id").asText();

        // Submit and verify university
        SubmitVerificationRequest univVerif = SubmitVerificationRequest.builder()
                .organizationId(UUID.fromString(univOrgId))
                .documents(List.of(SupportingDocumentRequest.builder()
                        .documentType(DocumentType.AISHE_CERTIFICATE)
                        .documentName("aishe.pdf")
                        .documentUrl("https://docs.bhu.ac.in/aishe.pdf")
                        .build()))
                .build();
        MvcResult uVerifRes = mockMvc.perform(post("/api/v1/verifications")
                        .header("Authorization", "Bearer " + facultyToken)
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(univVerif)))
                .andExpect(status().isCreated())
                .andReturn();
        String uVerifId = objectMapper.readTree(uVerifRes.getResponse().getContentAsString()).get("data").get("id").asText();

        mockMvc.perform(post("/api/v1/verifications/" + uVerifId + "/review")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ReviewVerificationRequest.builder()
                                .decision(VerificationStatus.VERIFIED)
                                .reviewerNotes("Verified BHU")
                                .build())))
                .andExpect(status().isOk());

        // Add Faculty Profile with expertise in water purification & arsenic
        FacultyProfileRequest facProfile = FacultyProfileRequest.builder()
                .organizationId(UUID.fromString(univOrgId))
                .departmentName("Department of Chemical & Environmental Engineering")
                .designation("Senior Professor & Dean of R&D")
                .primaryDiscipline("Environmental Chemistry")
                .researchAreas("water purification, arsenic remediation, membrane filtration, nanotechnology")
                .yearsOfExperience(18)
                .availableForMentorship(true)
                .build();
        mockMvc.perform(post("/api/v1/faculty/profile")
                        .header("Authorization", "Bearer " + facultyToken)
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(facProfile)))
                .andExpect(status().isOk());

        // Add Institutional Laboratory Resource
        InstitutionalResourceRequest labReq = InstitutionalResourceRequest.builder()
                .resourceName("Advanced Water Quality & Nanomaterial Lab")
                .resourceType(ResourceType.LABORATORY)
                .description("Facility for micro-contaminant detection and solar distillation")
                .accessibleToExternalTeams(true)
                .build();
        mockMvc.perform(post("/api/v1/organizations/" + univOrgId + "/resources")
                        .header("Authorization", "Bearer " + facultyToken)
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(labReq)))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("End-to-End: Submit Challenge -> Endorse -> Dept Triage -> Escalate to Innovation -> Academic Pipeline Match")
    void completeChallengeCrowdsourcingToAcademicEcosystem() throws Exception {
        // 1. Citizen Submits Societal Challenge with Evidence Attachment
        SubmitChallengeRequest submitReq = SubmitChallengeRequest.builder()
                .title("Severe Ground Water Arsenic Contamination in Chandauli Village Hand Pumps")
                .description("Over 1,200 villagers are suffering from waterborne arsenic toxicity. Standard hand pumps yield dark contaminated water. Urgent membrane filtration or solar water purification required.")
                .domainCode("WATER_SANITATION")
                .subCategory("Drinking Water Purification")
                .submitterType(SubmitterType.CITIZEN)
                .latitude(BigDecimal.valueOf(25.2638))
                .longitude(BigDecimal.valueOf(83.2652))
                .addressLine("Near Village Panchayat Bhavan")
                .locality("Chandauli Rural")
                .district("Chandauli")
                .state("Uttar Pradesh")
                .pincode("232101")
                .jurisdictionLevel(GovernmentLevel.PANCHAYAT_PRI)
                .severityLevel(SeverityLevel.CRITICAL)
                .urgencyLevel(UrgencyLevel.IMMEDIATE)
                .estimatedAffectedPopulation(1200)
                .attachments(List.of(
                        AttachmentRequest.builder()
                                .mediaType(MediaType.IMAGE)
                                .fileName("hand_pump_sample.jpg")
                                .fileUrl("https://media.samadhanx.org/evidence/sample01.jpg")
                                .caption("Discolored water from hand pump #4")
                                .build()
                ))
                .build();

        MvcResult submitResult = mockMvc.perform(post("/api/v1/challenges")
                        .header("Authorization", "Bearer " + citizenToken)
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(submitReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.trackingNumber", startsWith("SMX-")))
                .andExpect(jsonPath("$.data.domainCode", is("WATER_SANITATION")))
                .andExpect(jsonPath("$.data.priorityScore", greaterThan(70.0)))
                .andExpect(jsonPath("$.data.attachments", hasSize(1)))
                .andReturn();

        JsonNode challengeJson = objectMapper.readTree(submitResult.getResponse().getContentAsString()).get("data");
        String challengeId = challengeJson.get("id").asText();
        String trackingNumber = challengeJson.get("trackingNumber").asText();

        // 2. Endorse Challenge (Upvote)
        mockMvc.perform(post("/api/v1/challenges/" + challengeId + "/endorse")
                        .header("Authorization", "Bearer " + facultyToken)
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(EndorsementRequest.builder()
                                .comment("Confirmed by local community workers. Water quality test needed.")
                                .isAffected(true)
                                .build())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.isAffected", is(true)));

        // 3. Department Official Conducts Field Inspection
        DepartmentActionRequest inspectReq = DepartmentActionRequest.builder()
                .actionType(DepartmentActionType.FIELD_INSPECTION_COMPLETED)
                .fieldInspectionNotes("Official inspection conducted on site. Water sample verified at 0.082 mg/L arsenic.")
                .actionNotes("Standard chlorine and sand filtration insufficient for high dissolved arsenic.")
                .build();

        mockMvc.perform(post("/api/v1/challenges/" + challengeId + "/department-action")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inspectReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status", is("UNDER_DEPARTMENT_TRIAGE")));

        // 4. Department Escalates Challenge to Academic Innovation Pipeline
        EscalateToInnovationRequest escalateReq = EscalateToInnovationRequest.builder()
                .escalationJustification("Arsenic level exceeds permissible limits by 8x. Standard departmental budget cannot deploy standard RO pipelines. Novel low-cost graphene/clay nanomembrane filter and solar distillation needed from university engineering researchers.")
                .suggestedCapabilities("Nanomembrane filtration, solar water purification")
                .build();

        mockMvc.perform(post("/api/v1/challenges/" + challengeId + "/escalate-to-innovation")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(escalateReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status", is("INNOVATION_REQUIRED")))
                .andExpect(jsonPath("$.data.resolutionPath", is("INNOVATION_RESEARCH")));

        // 5. University Browses Open Innovation Pipeline
        mockMvc.perform(get("/api/v1/challenges/innovation-pipeline"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content", hasSize(greaterThanOrEqualTo(1))));

        // 6. University AI Matching Engine Recommends Challenge to Faculty Experts
        mockMvc.perform(get("/api/v1/challenges/matching-university/" + univOrgId)
                        .header("Authorization", "Bearer " + facultyToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$.data[0].matchScore", greaterThanOrEqualTo(70.0)))
                .andExpect(jsonPath("$.data[0].matchingFacultyExperts", hasSize(greaterThanOrEqualTo(1))));

        // 7. Verify Public Progress Timeline
        mockMvc.perform(get("/api/v1/challenges/" + challengeId + "/timeline"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(greaterThanOrEqualTo(3))));

        // 8. Public Tracking by Tracking Number
        mockMvc.perform(get("/api/v1/challenges/tracking/" + trackingNumber))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.trackingNumber", is(trackingNumber)))
                .andExpect(jsonPath("$.data.status", is("INNOVATION_REQUIRED")));
    }

    @Test
    @DisplayName("Department standard works resolution should mark challenge as RESOLVED_BY_DEPARTMENT")
    void shouldResolveViaStandardDepartmentWorks() throws Exception {
        SubmitChallengeRequest submitReq = SubmitChallengeRequest.builder()
                .title("Deep Potholes on Varanasi-Chandauli Link Road Causing Accidents")
                .description("Three large potholes near KM-14 causing severe road accidents for school buses and daily commuters.")
                .domainCode("URBAN_MOBILITY")
                .latitude(BigDecimal.valueOf(25.3176))
                .longitude(BigDecimal.valueOf(82.9739))
                .district("Varanasi")
                .state("Uttar Pradesh")
                .pincode("221001")
                .severityLevel(SeverityLevel.HIGH)
                .urgencyLevel(UrgencyLevel.HIGH)
                .estimatedAffectedPopulation(500)
                .build();

        MvcResult submitResult = mockMvc.perform(post("/api/v1/challenges")
                        .header("Authorization", "Bearer " + citizenToken)
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(submitReq)))
                .andExpect(status().isCreated())
                .andReturn();

        String challengeId = objectMapper.readTree(submitResult.getResponse().getContentAsString()).get("data").get("id").asText();

        // Department Resolves Challenge
        DepartmentResolveRequest resolveReq = DepartmentResolveRequest.builder()
                .resolutionSummary("Road repair team filled potholes using hot mix asphalt and leveled road surface.")
                .measurableImpactDescription("Zero accidents reported; smooth transit restored for 500+ daily vehicles.")
                .build();

        mockMvc.perform(post("/api/v1/challenges/" + challengeId + "/department-resolve")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(resolveReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status", is("RESOLVED_BY_DEPARTMENT")))
                .andExpect(jsonPath("$.data.resolutionPath", is("DEPARTMENTAL_STANDARD")))
                .andExpect(jsonPath("$.data.resolvedAt", notNullValue()));
    }
}
