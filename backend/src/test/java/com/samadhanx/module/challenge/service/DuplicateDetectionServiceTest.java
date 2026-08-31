package com.samadhanx.module.challenge.service;

import com.samadhanx.module.challenge.entity.Challenge;
import com.samadhanx.module.challenge.repository.ChallengeRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("DuplicateDetectionService Unit Tests")
class DuplicateDetectionServiceTest {

    @Mock
    private ChallengeRepository challengeRepository;

    @InjectMocks
    private DuplicateDetectionServiceImpl service;

    @Test
    @DisplayName("Should detect duplicate when challenge is within 2 km radius with matching keywords")
    void shouldDetectDuplicateWithinRadius() {
        UUID domainId = UUID.randomUUID();
        UUID existingId = UUID.randomUUID();

        Challenge existing = Challenge.builder()
                .id(existingId)
                .title("Arsenic contamination in drinking water hand pump")
                .description("Villagers facing water contamination and skin issues from hand pump.")
                .latitude(BigDecimal.valueOf(25.2638))
                .longitude(BigDecimal.valueOf(83.2652))
                .build();

        when(challengeRepository.findNearbyActiveInDomain(eq(domainId), any(), any(), any(), any()))
                .thenReturn(List.of(existing));

        // New challenge 100 meters away with identical context
        DuplicateDetectionService.DuplicateCheckResult result = service.checkForDuplicate(
                domainId,
                "Arsenic contamination in drinking water hand pump",
                "Villagers facing water contamination and skin issues from hand pump.",
                BigDecimal.valueOf(25.2640),
                BigDecimal.valueOf(83.2654)
        );

        assertNotNull(result);
        assertTrue(result.isDuplicate());
        assertEquals(existingId, result.parentChallengeId());
        assertTrue(result.similarityScore().doubleValue() >= 0.85);
    }
}
