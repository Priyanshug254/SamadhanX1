-- ================================================================
-- SamadhanX Reference Data — Milestone 1 & Milestone 2
-- ================================================================

-- ── 14 Platform Roles ─────────────────────────────────────────
INSERT INTO roles (name, description) VALUES
    ('CITIZEN',               'Individual citizen submitting community challenges')
ON CONFLICT (name) DO NOTHING;

INSERT INTO roles (name, description) VALUES
    ('COMMUNITY_ORGANIZATION','NGOs, self-help groups and community bodies')
ON CONFLICT (name) DO NOTHING;

INSERT INTO roles (name, description) VALUES
    ('GOVERNMENT_OFFICIAL',   'Government department officials managing assigned challenges')
ON CONFLICT (name) DO NOTHING;

INSERT INTO roles (name, description) VALUES
    ('GOVERNMENT_ADMIN',      'Senior government administrators with cross-department visibility')
ON CONFLICT (name) DO NOTHING;

INSERT INTO roles (name, description) VALUES
    ('UNIVERSITY_ADMIN',      'University administrators managing institution profile and teams')
ON CONFLICT (name) DO NOTHING;

INSERT INTO roles (name, description) VALUES
    ('FACULTY',               'University faculty members leading research and solution proposals')
ON CONFLICT (name) DO NOTHING;

INSERT INTO roles (name, description) VALUES
    ('STUDENT',               'University students participating in solution development teams')
ON CONFLICT (name) DO NOTHING;

INSERT INTO roles (name, description) VALUES
    ('INDUSTRY',              'Industry organizations providing mentorship, funding and expertise')
ON CONFLICT (name) DO NOTHING;

INSERT INTO roles (name, description) VALUES
    ('STARTUP',               'Startups contributing innovation and technology solutions')
ON CONFLICT (name) DO NOTHING;

INSERT INTO roles (name, description) VALUES
    ('MSME',                  'Micro, Small and Medium Enterprises offering practical implementation support')
ON CONFLICT (name) DO NOTHING;

INSERT INTO roles (name, description) VALUES
    ('CSR',                   'Corporate Social Responsibility organizations providing funding and resources')
ON CONFLICT (name) DO NOTHING;

INSERT INTO roles (name, description) VALUES
    ('RESEARCH_LAB',          'Research laboratories contributing domain expertise and facilities')
ON CONFLICT (name) DO NOTHING;

INSERT INTO roles (name, description) VALUES
    ('INNOVATION_HUB',        'Innovation and incubation hubs facilitating prototype and pilot programs')
ON CONFLICT (name) DO NOTHING;

INSERT INTO roles (name, description) VALUES
    ('SUPER_ADMIN',           'Platform super-administrators with full system access')
ON CONFLICT (name) DO NOTHING;

-- ── Societal Problem Domains / Sectors ─────────────────────────
INSERT INTO domains (code, name, description, is_active) VALUES
    ('WATER_SANITATION',     'Water & Sanitation',          'Drinking water purity, wastewater treatment, sanitation infrastructure and water table replenishment', true)
ON CONFLICT (code) DO NOTHING;

INSERT INTO domains (code, name, description, is_active) VALUES
    ('AGRI_TECH',            'Agriculture & Rural Tech',    'Smart irrigation, post-harvest preservation, crop disease detection and soil health enhancement', true)
ON CONFLICT (code) DO NOTHING;

INSERT INTO domains (code, name, description, is_active) VALUES
    ('CLEAN_ENERGY',         'Clean & Renewable Energy',    'Solar decentralization, biomass utilization, microgrid resilience and rural electrification', true)
ON CONFLICT (code) DO NOTHING;

INSERT INTO domains (code, name, description, is_active) VALUES
    ('HEALTHCARE',           'Healthcare & Public Hygiene', 'Rural diagnostic access, epidemic surveillance, telemedicine and maternal-child health', true)
ON CONFLICT (code) DO NOTHING;

INSERT INTO domains (code, name, description, is_active) VALUES
    ('URBAN_MOBILITY',       'Urban Mobility & Roads',      'Pothole monitoring, smart traffic routing, pedestrian safety and public transport efficiency', true)
ON CONFLICT (code) DO NOTHING;

INSERT INTO domains (code, name, description, is_active) VALUES
    ('WASTE_MGMT',           'Waste Management',            'Solid waste segregation, plastic recycling, electronic waste management and landfill diversion', true)
ON CONFLICT (code) DO NOTHING;

INSERT INTO domains (code, name, description, is_active) VALUES
    ('DISASTER_RESILIENCE',  'Disaster Resilience & Safety','Flood early warning, landslide prediction, cyclone preparedness and emergency response', true)
ON CONFLICT (code) DO NOTHING;

INSERT INTO domains (code, name, description, is_active) VALUES
    ('EDUCATION_SKILLING',   'Education & Livelihoods',     'Digital learning access for remote schools, vocational skilling and assistive tech for disabled', true)
ON CONFLICT (code) DO NOTHING;
