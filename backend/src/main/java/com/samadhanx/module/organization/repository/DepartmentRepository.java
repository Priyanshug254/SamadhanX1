package com.samadhanx.module.organization.repository;

import com.samadhanx.module.organization.entity.Department;
import com.samadhanx.module.organization.entity.enums.GovernmentLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, UUID> {

    List<Department> findByLevel(GovernmentLevel level);

    List<Department> findByParentDepartmentOrganizationId(UUID parentDepartmentId);

    @Query("SELECT d FROM Department d " +
            "JOIN FETCH d.organization o " +
            "LEFT JOIN FETCH d.problemCategories pc " +
            "WHERE d.organizationId = :id")
    Optional<Department> findByIdWithProblemCategories(@Param("id") UUID id);
}
