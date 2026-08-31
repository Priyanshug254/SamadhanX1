package com.samadhanx.module.challenge.repository;

import com.samadhanx.module.challenge.entity.ChallengeAttachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ChallengeAttachmentRepository extends JpaRepository<ChallengeAttachment, UUID> {
    List<ChallengeAttachment> findByChallengeId(UUID challengeId);
}
