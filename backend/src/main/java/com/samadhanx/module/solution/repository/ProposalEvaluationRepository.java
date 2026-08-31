package com.samadhanx.module.solution.repository;

import com.samadhanx.module.solution.entity.ProposalEvaluation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProposalEvaluationRepository extends JpaRepository<ProposalEvaluation, UUID> {

    List<ProposalEvaluation> findByProposalId(UUID proposalId);

    Optional<ProposalEvaluation> findByProposalIdAndEvaluatorId(UUID proposalId, UUID evaluatorId);

    boolean existsByProposalIdAndEvaluatorId(UUID proposalId, UUID evaluatorId);

    @Query("SELECT pe FROM ProposalEvaluation pe " +
            "JOIN FETCH pe.evaluator u " +
            "WHERE pe.proposal.id = :proposalId")
    List<ProposalEvaluation> findByProposalIdWithEvaluator(@Param("proposalId") UUID proposalId);

    List<ProposalEvaluation> findByEvaluatorId(UUID evaluatorId);

    long countByEvaluatorId(UUID evaluatorId);
}
