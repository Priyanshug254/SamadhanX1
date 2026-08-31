package com.samadhanx.module.organization.repository;

import com.samadhanx.module.organization.entity.VerificationRequest;
import com.samadhanx.module.organization.entity.enums.VerificationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface VerificationRequestRepository extends JpaRepository<VerificationRequest, UUID> {

    Page<VerificationRequest> findByStatus(VerificationStatus status, Pageable pageable);

    List<VerificationRequest> findByOrganizationIdOrderBySubmittedAtDesc(UUID organizationId);

    Optional<VerificationRequest> findFirstByOrganizationIdOrderBySubmittedAtDesc(UUID organizationId);

    @Query("SELECT vr FROM VerificationRequest vr " +
            "JOIN FETCH vr.organization o " +
            "JOIN FETCH vr.submittedBy u " +
            "LEFT JOIN FETCH vr.documents d " +
            "WHERE vr.id = :id")
    Optional<VerificationRequest> findByIdWithDetails(@Param("id") UUID id);
}
