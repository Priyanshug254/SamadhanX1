package com.samadhanx.module.solution.repository;

import com.samadhanx.module.solution.entity.Proposal;
import com.samadhanx.module.solution.entity.enums.ProposalStatus;
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
public interface ProposalRepository extends JpaRepository<Proposal, UUID>, JpaSpecificationExecutor<Proposal> {

    Optional<Proposal> findByTrackingNumber(String trackingNumber);

    List<Proposal> findByChallengeId(UUID challengeId);

    List<Proposal> findByTeamId(UUID teamId);

    Page<Proposal> findByStatus(ProposalStatus status, Pageable pageable);

    Page<Proposal> findByHackathonId(UUID hackathonId, Pageable pageable);

    @Query("SELECT p FROM Proposal p " +
            "JOIN FETCH p.challenge c " +
            "JOIN FETCH p.team t " +
            "JOIN FETCH p.submittedBy u " +
            "WHERE p.id = :id")
    Optional<Proposal> findByIdWithDetails(@Param("id") UUID id);

    @Query("SELECT p FROM Proposal p " +
            "WHERE p.challenge.id = :challengeId " +
            "ORDER BY p.averageScore DESC, p.submittedAt ASC")
    List<Proposal> findRankedProposalsForChallenge(@Param("challengeId") UUID challengeId);

    long countByStatus(ProposalStatus status);

    @Query("SELECT COUNT(p) FROM Proposal p WHERE p.team.homeUniversity.id = :universityId")
    long countByUniversityId(@Param("universityId") UUID universityId);

    @Query("SELECT COUNT(p) FROM Proposal p WHERE p.team.homeUniversity.id = :universityId AND p.status = :status")
    long countByUniversityIdAndStatus(@Param("universityId") UUID universityId, @Param("status") ProposalStatus status);
}
