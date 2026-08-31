-- ============================================================
-- SamadhanX: seed.sql
-- Seed Data for Roles and Societal Problem Domains
-- ============================================================

-- 1. Seed Roles
INSERT INTO public.roles (name, description, is_self_registerable) VALUES
    ('CITIZEN', 'Individual citizen reporting societal challenges and participating in community solutions', true),
    ('COMMUNITY_ORGANIZATION', 'NGO, local community group, or civic society organization', true),
    ('STUDENT', 'University or college student building solutions and participating in hackathons', true),
    ('FACULTY', 'Academic faculty member leading research, student teams, and mentoring projects', true),
    ('INDUSTRY', 'Commercial enterprise providing technology, scale, and deployment capabilities', true),
    ('STARTUP', 'Early or growth stage startup building innovative solutions', true),
    ('MSME', 'Micro, Small, and Medium Enterprises offering localized manufacturing and services', true),
    ('CSR', 'Corporate Social Responsibility or Philanthropic Funding Partner', true),
    ('RESEARCH_LAB', 'Academic or national research laboratory providing deep-tech expertise', true),
    ('INNOVATION_HUB', 'Incubator, accelerator, or prototyping center supporting pilots', true),
    ('GOVERNMENT_OFFICIAL', 'Departmental officer handling problem evaluation and departmental routing', false),
    ('GOVERNMENT_ADMIN', 'Head of government department or nodal administrative authority', false),
    ('UNIVERSITY_ADMIN', 'Dean or director overseeing university research and student teams', false),
    ('SUPER_ADMIN', 'Platform Administrator with complete system governance and verification rights', false)
ON CONFLICT (name) DO UPDATE SET
    description = EXCLUDED.description,
    is_self_registerable = EXCLUDED.is_self_registerable;

-- 2. Seed Societal Problem Domains
INSERT INTO public.domains (code, name, description, icon_name, is_active) VALUES
    ('WATER_SANITATION', 'Water & Sanitation', 'Drinking water accessibility, contamination detection, sewage, drainage, and urban sanitation', 'water_drop', true),
    ('AGRI_TECH', 'Agriculture & Rural Tech', 'Crop disease monitoring, irrigation management, soil health, and rural supply chain efficiency', 'agriculture', true),
    ('CLEAN_ENERGY', 'Clean & Renewable Energy', 'Solar/micro-grid installations, energy efficiency, clean cooking, and renewable power systems', 'solar_power', true),
    ('HEALTHCARE', 'Healthcare & Public Hygiene', 'Primary healthcare access, maternal care, epidemic tracking, diagnostic kits, and medical logistics', 'local_hospital', true),
    ('URBAN_MOBILITY', 'Urban Mobility & Roads', 'Pothole detection, smart traffic, public transport accessibility, and rural connectivity', 'directions_car', true),
    ('WASTE_MGMT', 'Waste Management', 'Solid waste segregation, e-waste collection, landfill minimization, and plastic recycling solutions', 'delete_sweep', true),
    ('DISASTER_RESILIENCE', 'Disaster Resilience & Safety', 'Early warning systems, flood mitigation, earthquake readiness, and emergency rescue coordination', 'shield', true),
    ('EDUCATION_SKILLING', 'Education & Livelihoods', 'Digital literacy, vocational skills training, rural education access, and youth livelihood creation', 'school', true)
ON CONFLICT (code) DO UPDATE SET
    name = EXCLUDED.name,
    description = EXCLUDED.description,
    icon_name = EXCLUDED.icon_name,
    is_active = EXCLUDED.is_active;
