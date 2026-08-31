package com.samadhanx.module.challenge.service;

import com.samadhanx.module.challenge.entity.enums.SeverityLevel;
import com.samadhanx.module.challenge.entity.enums.UrgencyLevel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("PriorityScoringService Unit Tests")
class PriorityScoringServiceTest {

    private final PriorityScoringService service = new PriorityScoringServiceImpl();

    @Test
    @DisplayName("Critical severity with immediate urgency and large population should yield high priority score (> 80.0)")
    void shouldComputeHighPriorityScore() {
        BigDecimal score = service.computePriorityScore(
                SeverityLevel.CRITICAL, // 100 * 0.35 = 35
                UrgencyLevel.IMMEDIATE, // 100 * 0.25 = 25
                5000,                   // 100 * 0.25 = 25
                15                      // min(100, 150) = 100 * 0.15 = 15
        );

        assertNotNull(score);
        assertEquals(100.00, score.doubleValue());
    }

    @Test
    @DisplayName("Low severity with low urgency and small population should yield low priority score (< 30.0)")
    void shouldComputeLowPriorityScore() {
        BigDecimal score = service.computePriorityScore(
                SeverityLevel.LOW, // 20 * 0.35 = 7
                UrgencyLevel.LOW,  // 20 * 0.25 = 5
                5,                 // 20 * 0.25 = 5
                0                  // 0 * 0.15 = 0
        );

        assertNotNull(score);
        assertEquals(17.00, score.doubleValue());
    }
}
