package com.samadhanx.module.challenge.service;

import com.samadhanx.module.challenge.entity.enums.SeverityLevel;
import com.samadhanx.module.challenge.entity.enums.UrgencyLevel;

import java.math.BigDecimal;

public interface PriorityScoringService {
    BigDecimal computePriorityScore(
            SeverityLevel severity,
            UrgencyLevel urgency,
            Integer affectedPopulation,
            Integer endorsementCount
    );
}
