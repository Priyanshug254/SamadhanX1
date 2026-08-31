package com.samadhanx.module.solution.repository;

import com.samadhanx.module.solution.entity.TeamMember;
import com.samadhanx.module.solution.entity.enums.TeamMemberStatus;
import com.samadhanx.module.solution.entity.enums.TeamRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TeamMemberRepository extends JpaRepository<TeamMember, UUID> {

    List<TeamMember> findByTeamId(UUID teamId);

    Optional<TeamMember> findByTeamIdAndUserId(UUID teamId, UUID userId);

    boolean existsByTeamIdAndUserId(UUID teamId, UUID userId);

    List<TeamMember> findByUserIdAndStatus(UUID userId, TeamMemberStatus status);

    @Query("SELECT tm FROM TeamMember tm " +
            "JOIN FETCH tm.user u " +
            "JOIN FETCH tm.university org " +
            "WHERE tm.team.id = :teamId")
    List<TeamMember> findByTeamIdWithDetails(@Param("teamId") UUID teamId);

    long countByTeamIdAndTeamRole(UUID teamId, TeamRole teamRole);
}
