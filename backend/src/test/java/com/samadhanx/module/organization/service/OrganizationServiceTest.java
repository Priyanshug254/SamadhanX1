package com.samadhanx.module.organization.service;

import com.samadhanx.common.exception.ConflictException;
import com.samadhanx.module.organization.dto.OrganizationResponse;
import com.samadhanx.module.organization.dto.RegisterOrganizationRequest;
import com.samadhanx.module.organization.entity.Organization;
import com.samadhanx.module.organization.entity.enums.OrganizationType;
import com.samadhanx.module.organization.entity.enums.VerificationStatus;
import com.samadhanx.module.organization.repository.DomainRepository;
import com.samadhanx.module.organization.repository.OrganizationDomainRepository;
import com.samadhanx.module.organization.repository.OrganizationMemberRepository;
import com.samadhanx.module.organization.repository.OrganizationRepository;
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
@DisplayName("OrganizationService Unit Tests")
class OrganizationServiceTest {

    @Mock
    private OrganizationRepository organizationRepository;

    @Mock
    private OrganizationMemberRepository organizationMemberRepository;

    @Mock
    private OrganizationDomainRepository organizationDomainRepository;

    @Mock
    private DomainRepository domainRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private OrganizationServiceImpl organizationService;

    private User creator;

    @BeforeEach
    void setUp() {
        creator = User.builder()
                .id(UUID.randomUUID())
                .email("creator@iitbhu.ac.in")
                .firstName("Registrar")
                .lastName("IIT BHU")
                .build();
    }

    @Test
    @DisplayName("Should successfully register a new university in PENDING_VERIFICATION state")
    void shouldRegisterUniversitySuccessfully() {
        RegisterOrganizationRequest request = RegisterOrganizationRequest.builder()
                .name("Indian Institute of Technology (BHU) Varanasi")
                .code("IIT-BHU")
                .organizationType(OrganizationType.UNIVERSITY)
                .contactEmail("contact@iitbhu.ac.in")
                .district("Varanasi")
                .state("Uttar Pradesh")
                .domainCodes(List.of("WATER_SANITATION"))
                .build();

        when(organizationRepository.existsByCodeIgnoreCase("IIT-BHU")).thenReturn(false);
        when(userRepository.findById(creator.getId())).thenReturn(Optional.of(creator));

        Organization savedOrg = Organization.builder()
                .id(UUID.randomUUID())
                .name(request.getName())
                .code("IIT-BHU")
                .organizationType(OrganizationType.UNIVERSITY)
                .contactEmail(request.getContactEmail())
                .district(request.getDistrict())
                .state(request.getState())
                .verificationStatus(VerificationStatus.PENDING_VERIFICATION)
                .build();

        when(organizationRepository.save(any(Organization.class))).thenReturn(savedOrg);

        OrganizationResponse response = organizationService.registerOrganization(request, creator.getId());

        assertNotNull(response);
        assertEquals("IIT-BHU", response.getCode());
        assertEquals(VerificationStatus.PENDING_VERIFICATION, response.getVerificationStatus());
        verify(organizationRepository, times(1)).save(any(Organization.class));
        verify(organizationMemberRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("Should reject organization registration when code already exists")
    void shouldThrowConflictWhenCodeExists() {
        RegisterOrganizationRequest request = RegisterOrganizationRequest.builder()
                .name("Duplicate Org")
                .code("DUPLICATE_CODE")
                .organizationType(OrganizationType.INDUSTRY)
                .contactEmail("info@duplicate.com")
                .district("Delhi")
                .state("Delhi")
                .build();

        when(organizationRepository.existsByCodeIgnoreCase("DUPLICATE_CODE")).thenReturn(true);

        assertThrows(ConflictException.class, () -> organizationService.registerOrganization(request, creator.getId()));
        verify(organizationRepository, never()).save(any());
    }
}
