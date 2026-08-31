package com.samadhanx.module.challenge.repository;

import com.samadhanx.module.challenge.entity.Challenge;
import com.samadhanx.module.challenge.entity.enums.ChallengeStatus;
import com.samadhanx.module.challenge.entity.enums.ResolutionPath;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ChallengeRepository extends JpaRepository<Challenge, UUID>, JpaSpecificationExecutor<Challenge> {

    Optional<Challenge> findByTrackingNumber(String trackingNumber);

    Page<Challenge> findByStatus(ChallengeStatus status, Pageable pageable);

    Page<Challenge> findByResolutionPath(ResolutionPath resolutionPath, Pageable pageable);

    Page<Challenge> findByAssignedDepartmentOrganizationId(UUID departmentId, Pageable pageable);

    Page<Challenge> findBySubmittedById(UUID userId, Pageable pageable);

    @Query("SELECT c FROM Challenge c " +
            "JOIN FETCH c.domain d " +
            "JOIN FETCH c.submittedBy u " +
            "LEFT JOIN FETCH c.assignedDepartment dept " +
            "LEFT JOIN FETCH dept.organization " +
            "WHERE c.id = :id")
    Optional<Challenge> findByIdWithDetails(@Param("id") UUID id);

    @Query("SELECT c FROM Challenge c " +
            "WHERE c.domain.id = :domainId " +
            "AND c.latitude BETWEEN :minLat AND :maxLat " +
            "AND c.longitude BETWEEN :minLng AND :maxLng " +
            "AND c.status NOT IN ('RESOLVED_BY_DEPARTMENT', 'CLOSED', 'REJECTED')")
    List<Challenge> findNearbyActiveInDomain(
            @Param("domainId") UUID domainId,
            @Param("minLat") BigDecimal minLat,
            @Param("maxLat") BigDecimal maxLat,
            @Param("minLng") BigDecimal minLng,
            @Param("maxLng") BigDecimal maxLng
    );

    @Query("SELECT c FROM Challenge c " +
            "WHERE c.status IN ('INNOVATION_REQUIRED', 'OPEN_FOR_ACADEMIC_PROPOSALS') " +
            "AND c.domain.id IN :domainIds")
    List<Challenge> findOpenForAcademicMatchingInDomains(@Param("domainIds") List<UUID> domainIds);

    long countByStatus(ChallengeStatus status);
}
