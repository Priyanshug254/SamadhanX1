package com.samadhanx.module.organization.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.samadhanx.module.auth.dto.LoginRequest;
import com.samadhanx.module.auth.dto.RegisterRequest;
import com.samadhanx.module.organization.dto.InstitutionalResourceRequest;
import com.samadhanx.module.organization.dto.RegisterOrganizationRequest;
import com.samadhanx.module.organization.dto.ReviewVerificationRequest;
import com.samadhanx.module.organization.dto.SubmitVerificationRequest;
import com.samadhanx.module.organization.dto.SupportingDocumentRequest;
import com.samadhanx.module.organization.dto.UniversityProfileRequest;
import com.samadhanx.module.organization.entity.enums.DocumentType;
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
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Organization & Verification Workflow Integration Tests")
class OrganizationWorkflowIntegrationTest {

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
    private String userToken;
    private String userEmail;

    @BeforeEach
    void setUp() throws Exception {
        long ts = System.currentTimeMillis();

        // 1. Create a Super Admin directly in DB for testing admin verification operations
        String adminEmail = "admin." + ts + "@samadhanx.gov.in";
        Role superAdminRole = roleRepository.findByName(RoleName.SUPER_ADMIN).orElseGet(() ->
                roleRepository.save(Role.builder().name(RoleName.SUPER_ADMIN).description("Super Admin").build())
        );

        User adminUser = User.builder()
                .email(adminEmail)
                .passwordHash(passwordEncoder.encode("AdminPass@123"))
                .firstName("Super")
                .lastName("Admin")
                .isActive(true)
                .build();
        adminUser.addRole(superAdminRole);
        userRepository.save(adminUser);

        // Login as Super Admin
        MvcResult adminLoginResult = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(LoginRequest.builder()
                                .email(adminEmail)
                                .password("AdminPass@123")
                                .build())))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode adminJson = objectMapper.readTree(adminLoginResult.getResponse().getContentAsString());
        adminToken = adminJson.get("data").get("accessToken").asText();

        // 2. Register normal user (University Lead)
        userEmail = "dean.rnd." + ts + "@iitbhu.ac.in";
        RegisterRequest registerReq = RegisterRequest.builder()
                .email(userEmail)
                .password("StrongPassword@123")
                .firstName("Ramesh")
                .lastName("Upadhyay")
                .role(RoleName.FACULTY)
                .build();

        MvcResult userRegResult = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerReq)))
                .andExpect(status().isCreated())
                .andReturn();
        JsonNode userJson = objectMapper.readTree(userRegResult.getResponse().getContentAsString());
        userToken = userJson.get("data").get("accessToken").asText();
    }

    @Test
    @DisplayName("End-to-End: Register University -> Add Labs -> Submit Verification -> Admin Review & Approve")
    void completeInstitutionalLifecycleWorkflow() throws Exception {
        String orgCode = "IIT-BHU-" + System.currentTimeMillis();

        // 1. Register new University Organization
        RegisterOrganizationRequest regOrgReq = RegisterOrganizationRequest.builder()
                .name("Indian Institute of Technology (BHU)")
                .code(orgCode)
                .organizationType(OrganizationType.UNIVERSITY)
                .contactEmail("contact@" + orgCode.toLowerCase() + ".ac.in")
                .contactPhone("+915422368106")
                .district("Varanasi")
                .state("Uttar Pradesh")
                .pincode("221005")
                .domainCodes(List.of("WATER_SANITATION", "CLEAN_ENERGY"))
                .primaryDomainCode("WATER_SANITATION")
                .build();

        MvcResult orgResult = mockMvc.perform(post("/api/v1/organizations")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(regOrgReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.code", is(orgCode)))
                .andExpect(jsonPath("$.data.verificationStatus", is("PENDING_VERIFICATION")))
                .andReturn();

        JsonNode orgJson = objectMapper.readTree(orgResult.getResponse().getContentAsString());
        String orgId = orgJson.get("data").get("id").asText();

        // 2. Add University HEI Profile (AISHE, Incubation centre)
        UniversityProfileRequest univProfileReq = UniversityProfileRequest.builder()
                .aisheCode("U-" + System.currentTimeMillis())
                .institutionType(InstitutionType.IIT_NIT_IIIT)
                .naacGrade("A++")
                .nirfRankRange("1-10")
                .hasIncubationCentre(true)
                .incubationCentreName("IIT BHU Discovery & Incubation Centre")
                .totalFacultyCount(520)
                .totalStudentCount(7100)
                .build();

        mockMvc.perform(put("/api/v1/organizations/" + orgId + "/university-profile")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(univProfileReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.hasIncubationCentre", is(true)))
                .andExpect(jsonPath("$.data.institutionType", is("IIT_NIT_IIIT")));

        // 3. Add Institutional Resource (Water Testing Lab)
        InstitutionalResourceRequest resourceReq = InstitutionalResourceRequest.builder()
                .resourceName("Environmental Hydrology & Water Purification Lab")
                .resourceType(ResourceType.LABORATORY)
                .description("Advanced analytical facility for micro-pollutant detection")
                .equipmentList("GC-MS, ICP-OES, Spectrophotometer")
                .accessibleToExternalTeams(true)
                .build();

        mockMvc.perform(post("/api/v1/organizations/" + orgId + "/resources")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(resourceReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.resourceName", is(resourceReq.getResourceName())))
                .andExpect(jsonPath("$.data.accessibleToExternalTeams", is(true)));

        // 4. Submit Verification Application with AISHE proof document
        SubmitVerificationRequest verifReq = SubmitVerificationRequest.builder()
                .organizationId(java.util.UUID.fromString(orgId))
                .documents(List.of(
                        SupportingDocumentRequest.builder()
                                .documentType(DocumentType.AISHE_CERTIFICATE)
                                .documentName("aishe_cert_2026.pdf")
                                .documentUrl("https://docs.samadhanx.org/proofs/aishe_cert.pdf")
                                .build()
                ))
                .build();

        MvcResult verifResult = mockMvc.perform(post("/api/v1/verifications")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(verifReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.status", is("PENDING_VERIFICATION")))
                .andReturn();

        JsonNode verifJson = objectMapper.readTree(verifResult.getResponse().getContentAsString());
        String verifId = verifJson.get("data").get("id").asText();

        // 5. Admin fetches verification queue
        mockMvc.perform(get("/api/v1/verifications")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content", hasSize(greaterThanOrEqualTo(1))));

        // 6. Admin approves verification -> VERIFIED
        ReviewVerificationRequest reviewReq = ReviewVerificationRequest.builder()
                .decision(VerificationStatus.VERIFIED)
                .reviewerNotes("AISHE accreditation and NIRF ranking verified via official MHRD database.")
                .build();

        mockMvc.perform(post("/api/v1/verifications/" + verifId + "/review")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reviewReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status", is("VERIFIED")));

        // 7. Verify organization profile now displays VERIFIED status with all nested details
        mockMvc.perform(get("/api/v1/organizations/" + orgId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.verificationStatus", is("VERIFIED")))
                .andExpect(jsonPath("$.data.universityProfile.hasIncubationCentre", is(true)))
                .andExpect(jsonPath("$.data.resourceCount", is(1)))
                .andExpect(jsonPath("$.data.memberCount", is(1)));

        // 8. Verify audit logs recorded the full lifecycle history
        mockMvc.perform(get("/api/v1/verifications/organization/" + orgId + "/audit-logs")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(greaterThanOrEqualTo(2)))); // SUBMITTED and APPROVED
    }

    @Test
    @DisplayName("Public search should filter active domains")
    void shouldListActiveDomains() throws Exception {
        mockMvc.perform(get("/api/v1/domains"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data", hasSize(greaterThanOrEqualTo(8))));
    }
}
