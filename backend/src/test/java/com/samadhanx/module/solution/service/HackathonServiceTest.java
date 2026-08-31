package com.samadhanx.module.solution.service;

import com.samadhanx.common.exception.BadRequestException;
import com.samadhanx.module.organization.entity.Domain;
import com.samadhanx.module.organization.entity.Organization;
import com.samadhanx.module.organization.repository.DomainRepository;
import com.samadhanx.module.organization.repository.OrganizationRepository;
import com.samadhanx.module.solution.dto.CreateHackathonRequest;
import com.samadhanx.module.solution.dto.HackathonResponse;
import com.samadhanx.module.solution.entity.Hackathon;
import com.samadhanx.module.solution.entity.enums.HackathonStatus;
import com.samadhanx.module.solution.repository.HackathonEvaluatorRepository;
import com.samadhanx.module.solution.repository.HackathonRepository;
import com.samadhanx.module.user.entity.User;
import com.samadhanx.module.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("HackathonService Unit Tests")
class HackathonServiceTest {

    @Mock
    private HackathonRepository hackathonRepository;

    @Mock
    private HackathonEvaluatorRepository hackathonEvaluatorRepository;

    @Mock
    private OrganizationRepository organizationRepository;

    @Mock
    private DomainRepository domainRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private HackathonServiceImpl hackathonService;

    private User admin;
    private Organization org;
    private Domain domain;

    @BeforeEach
    void setUp() {
        admin = User.builder().id(UUID.randomUUID()).email("admin@samadhanx.gov.in").build();
        org = Organization.builder().id(UUID.randomUUID()).name("Ministry of Jal Shakti").build();
        domain = Domain.builder().id(UUID.randomUUID()).name("Water & Sanitation").build();
    }

    @Test
    @DisplayName("Should create and publish hackathon event")
    void shouldCreateHackathon() {
        CreateHackathonRequest req = CreateHackathonRequest.builder()
                .title("National Jal Samadhan Hackathon 2026")
                .code("SMX-HACK-JAL-2026")
                .description("Groundwater innovation competition")
                .organizerOrgId(org.getId())
                .domainId(domain.getId())
                .submissionDeadline(Instant.now().plus(30, ChronoUnit.DAYS))
                .evaluationDeadline(Instant.now().plus(45, ChronoUnit.DAYS))
                .build();

        when(hackathonRepository.existsByCode("SMX-HACK-JAL-2026")).thenReturn(false);
        when(organizationRepository.findById(org.getId())).thenReturn(Optional.of(org));
        when(userRepository.findById(admin.getId())).thenReturn(Optional.of(admin));
        when(domainRepository.findById(domain.getId())).thenReturn(Optional.of(domain));
        when(hackathonRepository.save(any(Hackathon.class))).thenAnswer(invocation -> {
            Hackathon h = invocation.getArgument(0);
            h.setId(UUID.randomUUID());
            return h;
        });

        HackathonResponse response = hackathonService.createHackathon(req, admin.getId());

        assertNotNull(response);
        assertEquals("SMX-HACK-JAL-2026", response.getCode());
        assertEquals(HackathonStatus.OPEN_FOR_SUBMISSIONS, response.getStatus());
    }

    @Test
    @DisplayName("Should reject hackathon when submission deadline is after evaluation deadline")
    void shouldRejectInvalidDeadlines() {
        CreateHackathonRequest req = CreateHackathonRequest.builder()
                .title("Invalid Hackathon")
                .code("INVALID-HACK")
                .description("Invalid")
                .organizerOrgId(org.getId())
                .submissionDeadline(Instant.now().plus(45, ChronoUnit.DAYS))
                .evaluationDeadline(Instant.now().plus(30, ChronoUnit.DAYS))
                .build();

        assertThrows(BadRequestException.class, () ->
                hackathonService.createHackathon(req, admin.getId())
        );
    }
}
