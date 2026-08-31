package com.samadhanx.module.role.entity;

import java.util.Set;

/**
 * Enumeration of all roles supported across the SamadhanX ecosystem.
 * Serving Citizen/Field, Government, University, Industry, and Super Admin portals.
 */
public enum RoleName {
    // ── Citizen & Community ──
    CITIZEN,
    COMMUNITY_ORGANIZATION,

    // ── Government ──
    GOVERNMENT_OFFICIAL,
    GOVERNMENT_ADMIN,

    // ── Academia & Research ──
    UNIVERSITY_ADMIN,
    FACULTY,
    STUDENT,

    // ── Industry & Partners ──
    INDUSTRY,
    STARTUP,
    MSME,
    CSR,
    RESEARCH_LAB,
    INNOVATION_HUB,

    // ── Platform Management ──
    SUPER_ADMIN;

    /**
     * Set of roles that can be self-selected during public registration.
     * Privileged administrative roles (SUPER_ADMIN, GOVERNMENT_ADMIN, GOVERNMENT_OFFICIAL, UNIVERSITY_ADMIN)
     * are strictly disallowed from public self-registration.
     */
    private static final Set<RoleName> SELF_REGISTERABLE_ROLES = Set.of(
            CITIZEN,
            COMMUNITY_ORGANIZATION,
            STUDENT,
            FACULTY,
            INDUSTRY,
            STARTUP,
            MSME,
            CSR,
            RESEARCH_LAB,
            INNOVATION_HUB
    );

    public boolean isSelfRegisterable() {
        return SELF_REGISTERABLE_ROLES.contains(this);
    }
}
