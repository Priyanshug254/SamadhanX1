package com.samadhanx.module.challenge.service;

import com.samadhanx.module.organization.entity.Department;
import com.samadhanx.module.organization.entity.enums.GovernmentLevel;

import java.util.UUID;

public interface DepartmentRoutingEngine {

    record DepartmentRoutingResult(
            Department department,
            String routingRationale
    ) {}

    DepartmentRoutingResult findBestMatchingDepartment(
            UUID domainId,
            String state,
            String district,
            GovernmentLevel jurisdictionLevel
    );
}
