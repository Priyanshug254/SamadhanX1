package com.samadhanx;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * SamadhanX — Societal Challenge Crowdsourcing and Collaborative Problem-Solving Ecosystem.
 *
 * <p>This is the entry point for the modular monolith backend. All client portals
 * (Government, University, Industry, Citizen Mobile App, Super Admin) communicate
 * exclusively through this single backend via versioned REST APIs.
 *
 * <p>Architecture: Modular Monolith — modules are kept strictly separated by package
 * boundaries and can be extracted into independent services in future milestones.
 */
@SpringBootApplication
@EnableJpaAuditing(auditorAwareRef = "jpaAuditorAware")
public class SamadhanXApplication {

    public static void main(String[] args) {
        SpringApplication.run(SamadhanXApplication.class, args);
    }
}
