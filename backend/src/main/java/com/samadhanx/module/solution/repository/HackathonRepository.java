package com.samadhanx.module.solution.repository;

import com.samadhanx.module.solution.entity.Hackathon;
import com.samadhanx.module.solution.entity.enums.HackathonStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface HackathonRepository extends JpaRepository<Hackathon, UUID> {

    Optional<Hackathon> findByCode(String code);

    boolean existsByCode(String code);

    Page<Hackathon> findByStatus(HackathonStatus status, Pageable pageable);

    @Query("SELECT h FROM Hackathon h " +
            "JOIN FETCH h.organizerOrganization org " +
            "LEFT JOIN FETCH h.challenges c " +
            "WHERE h.id = :id")
    Optional<Hackathon> findByIdWithDetails(@Param("id") UUID id);
}
