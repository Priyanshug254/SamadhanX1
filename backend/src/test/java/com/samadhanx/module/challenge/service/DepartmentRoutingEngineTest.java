package com.samadhanx.module.challenge.service;

import com.samadhanx.module.organization.entity.Department;
import com.samadhanx.module.organization.entity.Domain;
import com.samadhanx.module.organization.entity.Organization;
import com.samadhanx.module.organization.entity.OrganizationDomain;
import com.samadhanx.module.organization.entity.enums.GovernmentLevel;
import com.samadhanx.module.organization.entity.enums.VerificationStatus;
import com.samadhanx.module.organization.repository.DepartmentRepository;
import com.samadhanx.module.organization.repository.OrganizationDomainRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("DepartmentRoutingEngine Unit Tests")
class DepartmentRoutingEngineTest {

    @Mock
    private DepartmentRepository departmentRepository;

    @Mock
    private OrganizationDomainRepository organizationDomainRepository;

    @InjectMocks
    private DepartmentRoutingEngineImpl routingEngine;

    @Test
    @DisplayName("Should route to direct local department matching domain and district")
    void shouldRouteToDirectLocalDepartment() {
        UUID domainId = UUID.randomUUID();
        UUID deptOrgId = UUID.randomUUID();

        Organization org = Organization.builder()
                .id(deptOrgId)
                .name("Chandauli Water Works & Sanitation Division")
                .district("Chandauli")
                .state("Uttar Pradesh")
                .verificationStatus(VerificationStatus.VERIFIED)
                .build();

        Department dept = Department.builder()
                .organizationId(deptOrgId)
                .organization(org)
                .level(GovernmentLevel.DISTRICT)
                .jurisdictionArea("Chandauli")
                .build();

        Domain domain = Domain.builder().id(domainId).code("WATER_SANITATION").build();
        OrganizationDomain mapping = OrganizationDomain.builder().organization(org).domain(domain).build();

        when(departmentRepository.findAll()).thenReturn(List.of(dept));
        when(organizationDomainRepository.findByOrganizationId(deptOrgId)).thenReturn(List.of(mapping));

        DepartmentRoutingEngine.DepartmentRoutingResult result = routingEngine.findBestMatchingDepartment(
                domainId, "Uttar Pradesh", "Chandauli", GovernmentLevel.PANCHAYAT_PRI
        );

        assertNotNull(result);
        assertNotNull(result.department());
        assertEquals("Chandauli Water Works & Sanitation Division", result.department().getOrganization().getName());
        assertTrue(result.routingRationale().contains("Direct local match"));
    }
}
