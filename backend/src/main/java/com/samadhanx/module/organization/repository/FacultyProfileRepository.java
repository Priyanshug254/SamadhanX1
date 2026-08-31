package com.samadhanx.module.organization.repository;

import com.samadhanx.module.organization.entity.FacultyProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FacultyProfileRepository extends JpaRepository<FacultyProfile, UUID> {

    Optional<FacultyProfile> findByUserId(UUID userId);

    boolean existsByUserId(UUID userId);

    List<FacultyProfile> findByOrganizationId(UUID organizationId);

    List<FacultyProfile> findByPrimaryDisciplineContainingIgnoreCase(String discipline);

    @Query("SELECT fp FROM FacultyProfile fp " +
            "JOIN FETCH fp.user u " +
            "JOIN FETCH fp.organization o " +
            "WHERE fp.organization.id = :organizationId")
    List<FacultyProfile> findByOrganizationIdWithDetails(@Param("organizationId") UUID organizationId);
}
