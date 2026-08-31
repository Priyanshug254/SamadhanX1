package com.samadhanx.module.challenge.service;

import com.samadhanx.module.challenge.entity.Challenge;
import com.samadhanx.module.challenge.repository.ChallengeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DuplicateDetectionServiceImpl implements DuplicateDetectionService {

    public static final double DUPLICATE_THRESHOLD = 0.85;
    public static final double SEARCH_RADIUS_KM = 2.0;
    private static final double DEG_PER_KM_LAT = 1.0 / 110.574;
    private static final double DEG_PER_KM_LNG = 1.0 / 111.320;

    private final ChallengeRepository challengeRepository;

    @Override
    public DuplicateCheckResult checkForDuplicate(
            UUID domainId,
            String title,
            String description,
            BigDecimal latitude,
            BigDecimal longitude
    ) {
        if (domainId == null || latitude == null || longitude == null) {
            return new DuplicateCheckResult(false, null, null, BigDecimal.ZERO);
        }

        double lat = latitude.doubleValue();
        double lng = longitude.doubleValue();

        BigDecimal minLat = BigDecimal.valueOf(lat - (SEARCH_RADIUS_KM * DEG_PER_KM_LAT));
        BigDecimal maxLat = BigDecimal.valueOf(lat + (SEARCH_RADIUS_KM * DEG_PER_KM_LAT));
        BigDecimal minLng = BigDecimal.valueOf(lng - (SEARCH_RADIUS_KM * DEG_PER_KM_LNG));
        BigDecimal maxLng = BigDecimal.valueOf(lng + (SEARCH_RADIUS_KM * DEG_PER_KM_LNG));

        List<Challenge> candidates = challengeRepository.findNearbyActiveInDomain(
                domainId, minLat, maxLat, minLng, maxLng
        );

        if (candidates.isEmpty()) {
            return new DuplicateCheckResult(false, null, null, BigDecimal.ZERO);
        }

        String targetText = (title + " " + description).toLowerCase(Locale.ROOT);
        Set<String> targetTokens = tokenize(targetText);

        Challenge bestMatch = null;
        double maxCombinedScore = 0.0;

        for (Challenge candidate : candidates) {
            double candLat = candidate.getLatitude().doubleValue();
            double candLng = candidate.getLongitude().doubleValue();
            double distKm = haversineKm(lat, lng, candLat, candLng);

            if (distKm <= SEARCH_RADIUS_KM) {
                String candText = (candidate.getTitle() + " " + candidate.getDescription()).toLowerCase(Locale.ROOT);
                Set<String> candTokens = tokenize(candText);
                double textSimilarity = jaccardSimilarity(targetTokens, candTokens);

                double spatialProximity = Math.max(0.0, 1.0 - (distKm / SEARCH_RADIUS_KM));
                double combinedScore = (textSimilarity * 0.70) + (spatialProximity * 0.30);

                if (combinedScore > maxCombinedScore) {
                    maxCombinedScore = combinedScore;
                    bestMatch = candidate;
                }
            }
        }

        if (maxCombinedScore >= DUPLICATE_THRESHOLD && bestMatch != null) {
            UUID clusterId = bestMatch.getClusterId() != null ? bestMatch.getClusterId() : bestMatch.getId();
            BigDecimal simScore = BigDecimal.valueOf(maxCombinedScore).setScale(3, RoundingMode.HALF_UP);
            return new DuplicateCheckResult(true, bestMatch.getId(), clusterId, simScore);
        }

        return new DuplicateCheckResult(false, null, null, BigDecimal.valueOf(maxCombinedScore).setScale(3, RoundingMode.HALF_UP));
    }

    private Set<String> tokenize(String text) {
        if (text == null) return Set.of();
        String[] words = text.replaceAll("[^a-zA-Z0-9\\s]", " ").split("\\s+");
        Set<String> tokens = new HashSet<>();
        for (String w : words) {
            if (w.length() > 2) {
                tokens.add(w);
            }
        }
        return tokens;
    }

    private double jaccardSimilarity(Set<String> set1, Set<String> set2) {
        if (set1.isEmpty() || set2.isEmpty()) return 0.0;
        Set<String> intersection = new HashSet<>(set1);
        intersection.retainAll(set2);
        Set<String> union = new HashSet<>(set1);
        union.addAll(set2);
        return (double) intersection.size() / union.size();
    }

    private double haversineKm(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // Earth radius in km
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }
}
