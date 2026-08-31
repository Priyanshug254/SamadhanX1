package com.samadhanx.module.challenge.service;

import com.samadhanx.module.organization.entity.Department;
import com.samadhanx.module.organization.entity.enums.GovernmentLevel;
import com.samadhanx.module.organization.entity.enums.VerificationStatus;
import com.samadhanx.module.organization.repository.DepartmentRepository;
import com.samadhanx.module.organization.repository.OrganizationDomainRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DepartmentRoutingEngineImpl implements DepartmentRoutingEngine {

    private final DepartmentRepository departmentRepository;
    private final OrganizationDomainRepository organizationDomainRepository;

    @Override
    public DepartmentRoutingResult findBestMatchingDepartment(
            UUID domainId,
            String state,
            String district,
            GovernmentLevel jurisdictionLevel
    ) {
        List<Department> verifiedDepts = departmentRepository.findAll().stream()
                .filter(d -> d.getOrganization() != null &&
                        d.getOrganization().getVerificationStatus() == VerificationStatus.VERIFIED)
                .toList();

        if (verifiedDepts.isEmpty()) {
            return new DepartmentRoutingResult(null, "No verified government departments currently registered in the platform; queued for admin triage");
        }

        // Priority 1: Match Domain + Exact District + Exact State
        for (Department dept : verifiedDepts) {
            if (dept.getOrganization().getState().equalsIgnoreCase(state) &&
                    dept.getOrganization().getDistrict().equalsIgnoreCase(district) &&
                    isDeptInDomain(dept.getOrganizationId(), domainId)) {
                String rationale = String.format("Direct local match: Department '%s' specializes in this societal sector within district '%s', %s.",
                        dept.getOrganization().getName(), district, state);
                return new DepartmentRoutingResult(dept, rationale);
            }
        }

        // Priority 2: Match Domain + Same State
        for (Department dept : verifiedDepts) {
            if (dept.getOrganization().getState().equalsIgnoreCase(state) &&
                    isDeptInDomain(dept.getOrganizationId(), domainId)) {
                String rationale = String.format("State sector match: State department '%s' specializes in this domain across %s.",
                        dept.getOrganization().getName(), state);
                return new DepartmentRoutingResult(dept, rationale);
            }
        }

        // Priority 3: Match District + State (any verified department in locality)
        for (Department dept : verifiedDepts) {
            if (dept.getOrganization().getState().equalsIgnoreCase(state) &&
                    dept.getOrganization().getDistrict().equalsIgnoreCase(district)) {
                String rationale = String.format("Local administrative match: Local body '%s' in district '%s', %s.",
                        dept.getOrganization().getName(), district, state);
                return new DepartmentRoutingResult(dept, rationale);
            }
        }

        // Priority 4: Return any verified department in the same state
        for (Department dept : verifiedDepts) {
            if (dept.getOrganization().getState().equalsIgnoreCase(state)) {
                String rationale = String.format("State jurisdiction match: State department '%s' in %s.",
                        dept.getOrganization().getName(), state);
                return new DepartmentRoutingResult(dept, rationale);
            }
        }

        // Fallback: First verified department in platform
        Department fallback = verifiedDepts.get(0);
        return new DepartmentRoutingResult(fallback, "General platform routing fallback to primary verified department: " + fallback.getOrganization().getName());
    }

    private boolean isDeptInDomain(UUID orgId, UUID domainId) {
        if (domainId == null) return true;
        return organizationDomainRepository.findByOrganizationId(orgId).stream()
                .anyMatch(od -> od.getDomain().getId().equals(domainId));
    }
}
