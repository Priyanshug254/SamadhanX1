package com.samadhanx.module.organization.service;

import com.samadhanx.common.exception.BadRequestException;
import com.samadhanx.common.exception.ForbiddenException;
import com.samadhanx.module.organization.dto.ReviewVerificationRequest;
import com.samadhanx.module.organization.dto.SubmitVerificationRequest;
import com.samadhanx.module.organization.dto.SupportingDocumentRequest;
import com.samadhanx.module.organization.dto.SuspendOrganizationRequest;
import com.samadhanx.module.organization.dto.VerificationRequestResponse;
import com.samadhanx.module.organization.entity.Organization;
import com.samadhanx.module.organization.entity.VerificationRequest;
import com.samadhanx.module.organization.entity.enums.DocumentType;
import com.samadhanx.module.organization.entity.enums.OrganizationType;
import com.samadhanx.module.organization.entity.enums.VerificationStatus;
import com.samadhanx.module.organization.repository.OrganizationMemberRepository;
import com.samadhanx.module.organization.repository.OrganizationRepository;
import com.samadhanx.module.organization.repository.SupportingDocumentRepository;
import com.samadhanx.module.organization.repository.VerificationAuditLogRepository;
import com.samadhanx.module.organization.repository.VerificationRequestRepository;
import com.samadhanx.module.role.entity.Role;
import com.samadhanx.module.role.entity.RoleName;
import com.samadhanx.module.user.entity.User;
import com.samadhanx.module.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("VerificationService Unit Tests")
class VerificationServiceTest {

    @Mock
    private VerificationRequestRepository verificationRequestRepository;

    @Mock
    private SupportingDocumentRepository supportingDocumentRepository;

    @Mock
    private VerificationAuditLogRepository verificationAuditLogRepository;

    @Mock
    private OrganizationRepository organizationRepository;

    @Mock
    private OrganizationMemberRepository organizationMemberRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private VerificationServiceImpl verificationService;

    private Organization organization;
    private User submitter;
    private User superAdmin;

    @BeforeEach
    void setUp() {
        UUID orgId = UUID.randomUUID();
        organization = Organization.builder()
                .id(orgId)
                .name("Innovate Labs Ltd")
                .code("INNOVATE-LABS")
                .organizationType(OrganizationType.STARTUP)
                .contactEmail("contact@innovate.io")
                .district("Bengaluru Urban")
                .state("Karnataka")
                .verificationStatus(VerificationStatus.PENDING_VERIFICATION)
                .build();

        submitter = User.builder()
                .id(UUID.randomUUID())
                .email("founder@innovate.io")
                .firstName("Ananya")
                .lastName("Rao")
                .build();

        Role adminRole = Role.builder().name(RoleName.SUPER_ADMIN).build();
        superAdmin = User.builder()
                .id(UUID.randomUUID())
                .email("admin@samadhanx.gov.in")
                .firstName("Platform")
                .lastName("Admin")
                .build();
        superAdmin.addRole(adminRole);
    }

    @Test
    @DisplayName("Should submit verification request with supporting documents")
    void shouldSubmitVerificationSuccessfully() {
        SubmitVerificationRequest request = SubmitVerificationRequest.builder()
                .organizationId(organization.getId())
                .documents(List.of(
                        SupportingDocumentRequest.builder()
                                .documentType(DocumentType.DPIIT_CERTIFICATE)
                                .documentName("dpiit_cert.pdf")
                                .documentUrl("https://docs.samadhanx.org/dpiit.pdf")
                                .build()
                ))
                .build();

        when(organizationRepository.findById(organization.getId())).thenReturn(Optional.of(organization));
        when(userRepository.findById(submitter.getId())).thenReturn(Optional.of(submitter));
        when(organizationMemberRepository.existsByOrganizationIdAndUserId(organization.getId(), submitter.getId())).thenReturn(true);

        VerificationRequest savedVr = VerificationRequest.builder()
                .id(UUID.randomUUID())
                .organization(organization)
                .status(VerificationStatus.PENDING_VERIFICATION)
                .submittedBy(submitter)
                .build();

        when(verificationRequestRepository.save(any(VerificationRequest.class))).thenReturn(savedVr);

        VerificationRequestResponse response = verificationService.submitVerificationRequest(request, submitter.getId());

        assertNotNull(response);
        assertEquals(VerificationStatus.PENDING_VERIFICATION, response.getStatus());
        verify(verificationAuditLogRepository, times(1)).save(any());
        verify(supportingDocumentRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("Should approve verification request and update organization to VERIFIED")
    void shouldApproveVerificationRequest() {
        UUID reqId = UUID.randomUUID();
        VerificationRequest vr = VerificationRequest.builder()
                .id(reqId)
                .organization(organization)
                .status(VerificationStatus.UNDER_REVIEW)
                .submittedBy(submitter)
                .build();

        ReviewVerificationRequest reviewReq = ReviewVerificationRequest.builder()
                .decision(VerificationStatus.VERIFIED)
                .reviewerNotes("All DPIIT and CIN documents verified.")
                .build();

        when(verificationRequestRepository.findById(reqId)).thenReturn(Optional.of(vr));
        when(userRepository.findById(superAdmin.getId())).thenReturn(Optional.of(superAdmin));

        VerificationRequestResponse response = verificationService.reviewVerificationRequest(reqId, reviewReq, superAdmin.getId());

        assertNotNull(response);
        assertEquals(VerificationStatus.VERIFIED, organization.getVerificationStatus());
        assertNotNull(organization.getVerifiedAt());
        assertEquals(superAdmin.getId(), organization.getVerifiedBy());
        verify(verificationAuditLogRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("Should require rejection reason when rejecting verification request")
    void shouldRequireRejectionReasonOnReject() {
        UUID reqId = UUID.randomUUID();
        VerificationRequest vr = VerificationRequest.builder()
                .id(reqId)
                .organization(organization)
                .status(VerificationStatus.UNDER_REVIEW)
                .build();

        ReviewVerificationRequest reviewReq = ReviewVerificationRequest.builder()
                .decision(VerificationStatus.REJECTED)
                .rejectionReason("") // Empty reason
                .build();

        when(verificationRequestRepository.findById(reqId)).thenReturn(Optional.of(vr));
        when(userRepository.findById(superAdmin.getId())).thenReturn(Optional.of(superAdmin));

        assertThrows(BadRequestException.class, () ->
                verificationService.reviewVerificationRequest(reqId, reviewReq, superAdmin.getId()));
    }

    @Test
    @DisplayName("Should reject suspension attempt by non-SUPER_ADMIN user")
    void shouldRejectSuspensionByNonAdmin() {
        SuspendOrganizationRequest request = SuspendOrganizationRequest.builder()
                .reason("Fake organization")
                .build();

        when(organizationRepository.findById(organization.getId())).thenReturn(Optional.of(organization));
        when(userRepository.findById(submitter.getId())).thenReturn(Optional.of(submitter));

        assertThrows(ForbiddenException.class, () ->
                verificationService.suspendOrganization(organization.getId(), request, submitter.getId()));
    }
}
