package com.samadhanx.module.challenge.repository;

import com.samadhanx.module.challenge.entity.ChallengeDepartmentAction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ChallengeDepartmentActionRepository extends JpaRepository<ChallengeDepartmentAction, UUID> {

    @Query("SELECT cda FROM ChallengeDepartmentAction cda " +
            "JOIN FETCH cda.performedBy u " +
            "JOIN FETCH cda.department d " +
            "WHERE cda.challenge.id = :challengeId " +
            "ORDER BY cda.createdAt DESC")
    List<ChallengeDepartmentAction> findByChallengeIdOrderByCreatedAtDesc(@Param("challengeId") UUID challengeId);
}
