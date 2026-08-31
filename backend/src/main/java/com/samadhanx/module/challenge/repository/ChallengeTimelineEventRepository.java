package com.samadhanx.module.challenge.repository;

import com.samadhanx.module.challenge.entity.ChallengeTimelineEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ChallengeTimelineEventRepository extends JpaRepository<ChallengeTimelineEvent, UUID> {

    @Query("SELECT cte FROM ChallengeTimelineEvent cte " +
            "JOIN FETCH cte.actor u " +
            "WHERE cte.challenge.id = :challengeId " +
            "ORDER BY cte.createdAt ASC")
    List<ChallengeTimelineEvent> findByChallengeIdOrderByCreatedAtAsc(@Param("challengeId") UUID challengeId);

    @Query("SELECT cte FROM ChallengeTimelineEvent cte " +
            "JOIN FETCH cte.actor u " +
            "WHERE cte.challenge.id = :challengeId " +
            "AND cte.isPublic = true " +
            "ORDER BY cte.createdAt ASC")
    List<ChallengeTimelineEvent> findPublicTimelineByChallengeId(@Param("challengeId") UUID challengeId);
}
