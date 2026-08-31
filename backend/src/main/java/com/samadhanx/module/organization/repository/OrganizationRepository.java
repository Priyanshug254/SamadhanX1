package com.samadhanx.module.organization.repository;

import com.samadhanx.module.organization.entity.Organization;
import com.samadhanx.module.organization.entity.enums.OrganizationType;
import com.samadhanx.module.organization.entity.enums.VerificationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrganizationRepository extends JpaRepository<Organization, UUID>, JpaSpecificationExecutor<Organization> {

    Optional<Organization> findByCodeIgnoreCase(String code);

    boolean existsByCodeIgnoreCase(String code);

    Page<Organization> findByVerificationStatus(VerificationStatus verificationStatus, Pageable pageable);

    Page<Organization> findByOrganizationTypeAndVerificationStatus(
            OrganizationType organizationType,
            VerificationStatus verificationStatus,
            Pageable pageable
    );

    @Query("SELECT o FROM Organization o " +
            "LEFT JOIN FETCH o.domainMappings dm " +
            "LEFT JOIN FETCH dm.domain " +
            "WHERE o.id = :id")
    Optional<Organization> findByIdWithDomains(@Param("id") UUID id);

    @Query("SELECT o FROM Organization o " +
            "JOIN o.members m " +
            "WHERE m.user.id = :userId")
    List<Organization> findByUserId(@Param("userId") UUID userId);

    default List<Organization> findOrganizationsByUserId(UUID userId) {
        return findByUserId(userId);
    }

    long countByOrganizationType(OrganizationType organizationType);
}
