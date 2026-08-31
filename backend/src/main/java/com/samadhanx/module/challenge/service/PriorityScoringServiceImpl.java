package com.samadhanx.module.challenge.service;

import com.samadhanx.module.challenge.entity.enums.SeverityLevel;
import com.samadhanx.module.challenge.entity.enums.UrgencyLevel;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class PriorityScoringServiceImpl implements PriorityScoringService {

    private static final double WEIGHT_SEVERITY = 0.35;
    private static final double WEIGHT_URGENCY = 0.25;
    private static final double WEIGHT_POPULATION = 0.25;
    private static final double WEIGHT_ENDORSEMENT = 0.15;

    @Override
    public BigDecimal computePriorityScore(
            SeverityLevel severity,
            UrgencyLevel urgency,
            Integer affectedPopulation,
            Integer endorsementCount
    ) {
        int sevWeight = severity != null ? severity.getWeight() : 50;
        int urgWeight = urgency != null ? urgency.getWeight() : 50;
        int popWeight = calculatePopulationWeight(affectedPopulation != null ? affectedPopulation : 0);
        int endWeight = calculateEndorsementWeight(endorsementCount != null ? endorsementCount : 0);

        double composite = (sevWeight * WEIGHT_SEVERITY) +
                (urgWeight * WEIGHT_URGENCY) +
                (popWeight * WEIGHT_POPULATION) +
                (endWeight * WEIGHT_ENDORSEMENT);

        composite = Math.max(0.0, Math.min(100.0, composite));
        return BigDecimal.valueOf(composite).setScale(2, RoundingMode.HALF_UP);
    }

    private int calculatePopulationWeight(int pop) {
        if (pop <= 10) return 20;
        if (pop <= 100) return 40;
        if (pop <= 500) return 60;
        if (pop <= 2000) return 80;
        return 100;
    }

    private int calculateEndorsementWeight(int endorsements) {
        return Math.min(100, endorsements * 10);
    }
}
