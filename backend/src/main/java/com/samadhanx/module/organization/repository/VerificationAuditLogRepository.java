package com.samadhanx.module.organization.repository;

import com.samadhanx.module.organization.entity.VerificationAuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface VerificationAuditLogRepository extends JpaRepository<VerificationAuditLog, UUID> {

    @Query("SELECT val FROM VerificationAuditLog val " +
            "JOIN FETCH val.actionBy u " +
            "WHERE val.organization.id = :organizationId " +
            "ORDER BY val.createdAt DESC")
    List<VerificationAuditLog> findByOrganizationIdOrderByCreatedAtDesc(@Param("organizationId") UUID organizationId);

    List<VerificationAuditLog> findByVerificationRequestIdOrderByCreatedAtDesc(UUID verificationRequestId);
}
