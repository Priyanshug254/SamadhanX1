package com.samadhanx.infrastructure.init;

import com.samadhanx.module.organization.entity.Domain;
import com.samadhanx.module.organization.repository.DomainRepository;
import com.samadhanx.module.role.entity.Role;
import com.samadhanx.module.role.entity.RoleName;
import com.samadhanx.module.role.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

/**
 * Initializes reference data (roles & domains) on startup if not already present.
 * Ensures the system is ready without seeding any insecure default user credentials.
 */
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final RoleRepository roleRepository;
    private final DomainRepository domainRepository;

    @Override
    @Transactional
    public void run(String... args) {
        initializeRoles();
        initializeDomains();
    }

    private void initializeRoles() {
        log.info("Initializing SamadhanX reference roles...");

        Arrays.stream(RoleName.values()).forEach(roleName -> {
            if (!roleRepository.existsByName(roleName)) {
                Role role = Role.builder()
                        .name(roleName)
                        .description(getRoleDescription(roleName))
                        .build();
                roleRepository.save(role);
                log.info("Bootstrap created role: {}", roleName);
            }
        });

        log.info("SamadhanX role initialization complete. Total roles: {}", roleRepository.count());
    }

    private void initializeDomains() {
        log.info("Initializing SamadhanX societal problem domains...");

        List<DomainSeed> defaultDomains = List.of(
                new DomainSeed("WATER_SANITATION", "Water & Sanitation", "Drinking water purity, wastewater treatment, sanitation infrastructure and water table replenishment"),
                new DomainSeed("AGRI_TECH", "Agriculture & Rural Tech", "Smart irrigation, post-harvest preservation, crop disease detection and soil health enhancement"),
                new DomainSeed("CLEAN_ENERGY", "Clean & Renewable Energy", "Solar decentralization, biomass utilization, microgrid resilience and rural electrification"),
                new DomainSeed("HEALTHCARE", "Healthcare & Public Hygiene", "Rural diagnostic access, epidemic surveillance, telemedicine and maternal-child health"),
                new DomainSeed("URBAN_MOBILITY", "Urban Mobility & Roads", "Pothole monitoring, smart traffic routing, pedestrian safety and public transport efficiency"),
                new DomainSeed("WASTE_MGMT", "Waste Management", "Solid waste segregation, plastic recycling, electronic waste management and landfill diversion"),
                new DomainSeed("DISASTER_RESILIENCE", "Disaster Resilience & Safety", "Flood early warning, landslide prediction, cyclone preparedness and emergency response"),
                new DomainSeed("EDUCATION_SKILLING", "Education & Livelihoods", "Digital learning access for remote schools, vocational skilling and assistive tech for disabled")
        );

        for (DomainSeed seed : defaultDomains) {
            if (!domainRepository.existsByCode(seed.code)) {
                Domain domain = Domain.builder()
                        .code(seed.code)
                        .name(seed.name)
                        .description(seed.description)
                        .active(true)
                        .build();
                domainRepository.save(domain);
                log.info("Bootstrap created domain: {}", seed.code);
            }
        }

        log.info("SamadhanX domain initialization complete. Total active domains: {}", domainRepository.count());
    }

    private String getRoleDescription(RoleName roleName) {
        return switch (roleName) {
            case CITIZEN -> "Individual citizen submitting community challenges";
            case COMMUNITY_ORGANIZATION -> "NGOs, self-help groups and community bodies";
            case GOVERNMENT_OFFICIAL -> "Government department officials managing assigned challenges";
            case GOVERNMENT_ADMIN -> "Senior government administrators with cross-department visibility";
            case UNIVERSITY_ADMIN -> "University administrators managing institution profile and teams";
            case FACULTY -> "University faculty members leading research and solution proposals";
            case STUDENT -> "University students participating in solution development teams";
            case INDUSTRY -> "Industry organizations providing mentorship, funding and expertise";
            case STARTUP -> "Startups contributing innovation and technology solutions";
            case MSME -> "Micro, Small and Medium Enterprises offering practical implementation support";
            case CSR -> "Corporate Social Responsibility organizations providing funding and resources";
            case RESEARCH_LAB -> "Research laboratories contributing domain expertise and facilities";
            case INNOVATION_HUB -> "Innovation and incubation hubs facilitating prototype and pilot programs";
            case SUPER_ADMIN -> "Platform super-administrators with full system access";
        };
    }

    private record DomainSeed(String code, String name, String description) {}
}
