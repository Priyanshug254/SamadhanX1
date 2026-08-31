package com.samadhanx.module.challenge.service;

import com.samadhanx.module.challenge.entity.Challenge;

import java.math.BigDecimal;
import java.util.UUID;

public interface DuplicateDetectionService {

    record DuplicateCheckResult(
            boolean isDuplicate,
            UUID parentChallengeId,
            UUID clusterId,
            BigDecimal similarityScore
    ) {}

    DuplicateCheckResult checkForDuplicate(
            UUID domainId,
            String title,
            String description,
            BigDecimal latitude,
            BigDecimal longitude
    );
}
