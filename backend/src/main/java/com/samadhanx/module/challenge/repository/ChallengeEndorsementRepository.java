package com.samadhanx.module.challenge.repository;

import com.samadhanx.module.challenge.entity.ChallengeEndorsement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ChallengeEndorsementRepository extends JpaRepository<ChallengeEndorsement, UUID> {
    List<ChallengeEndorsement> findByChallengeId(UUID challengeId);
    Optional<ChallengeEndorsement> findByChallengeIdAndUserId(UUID challengeId, UUID userId);
    boolean existsByChallengeIdAndUserId(UUID challengeId, UUID userId);
    int countByChallengeId(UUID challengeId);
}
