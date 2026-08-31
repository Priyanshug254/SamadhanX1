package com.samadhanx.module.solution.repository;

import com.samadhanx.module.solution.entity.Team;
import com.samadhanx.module.solution.entity.enums.TeamStatus;
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
public interface TeamRepository extends JpaRepository<Team, UUID> {

    List<Team> findByChallengeId(UUID challengeId);

    Page<Team> findByHomeUniversityId(UUID universityId, Pageable pageable);

    Page<Team> findByStatus(TeamStatus status, Pageable pageable);

    @Query("SELECT t FROM Team t " +
            "JOIN FETCH t.homeUniversity u " +
            "JOIN FETCH t.challenge c " +
            "LEFT JOIN FETCH t.members m " +
            "WHERE t.id = :id")
    Optional<Team> findByIdWithDetails(@Param("id") UUID id);

    @Query("SELECT DISTINCT t FROM Team t " +
            "JOIN t.members m " +
            "WHERE m.user.id = :userId AND m.status = 'ACTIVE'")
    List<Team> findActiveTeamsForUser(@Param("userId") UUID userId);

    long countByHomeUniversityId(UUID universityId);
}
