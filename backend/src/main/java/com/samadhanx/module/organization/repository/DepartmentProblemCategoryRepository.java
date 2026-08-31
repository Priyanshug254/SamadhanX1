package com.samadhanx.module.organization.repository;

import com.samadhanx.module.organization.entity.DepartmentProblemCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DepartmentProblemCategoryRepository extends JpaRepository<DepartmentProblemCategory, UUID> {
    List<DepartmentProblemCategory> findByDepartmentOrganizationId(UUID departmentId);
}
