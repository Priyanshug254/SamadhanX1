-- ================================================================
-- SamadhanX Database Schema — Milestones 1, 2 & 3
-- ================================================================
-- This DDL is applied on startup in dev (CREATE TABLE IF NOT EXISTS).
-- All timestamps stored in UTC.
-- ================================================================

-- ── Roles ────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS roles (
    id          UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    name        VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255),
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- ── Users ────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS users (
    id                UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    email             VARCHAR(255) NOT NULL UNIQUE,
    password_hash     VARCHAR(255) NOT NULL,
    first_name        VARCHAR(100) NOT NULL,
    last_name         VARCHAR(100) NOT NULL,
    phone_number      VARCHAR(20),
    is_active         BOOLEAN     NOT NULL DEFAULT TRUE,
    is_email_verified BOOLEAN     NOT NULL DEFAULT FALSE,

    -- Audit fields
    created_at        TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at        TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by        UUID,
    updated_by        UUID
);

-- ── User ↔ Role (many-to-many) ────────────────────────────────
CREATE TABLE IF NOT EXISTS user_roles (
    id          UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id     UUID        NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role_id     UUID        NOT NULL REFERENCES roles(id) ON DELETE RESTRICT,
    assigned_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_user_role UNIQUE (user_id, role_id)
);

-- ── Domains / Sectors ─────────────────────────────────────────
CREATE TABLE IF NOT EXISTS domains (
    id          UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    code        VARCHAR(50) NOT NULL UNIQUE,
    name        VARCHAR(100) NOT NULL,
    description VARCHAR(255),
    is_active   BOOLEAN     NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by  UUID,
    updated_by  UUID
);

-- ── Organizations (Master Entity) ────────────────────────────
CREATE TABLE IF NOT EXISTS organizations (
    id                  UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    name                VARCHAR(255) NOT NULL,
    code                VARCHAR(100) NOT NULL UNIQUE,
    organization_type   VARCHAR(50) NOT NULL,
    description         TEXT,
    website             VARCHAR(255),
    contact_email       VARCHAR(255) NOT NULL,
    contact_phone       VARCHAR(20),
    address_line        VARCHAR(255),
    district            VARCHAR(100) NOT NULL,
    state               VARCHAR(100) NOT NULL,
    pincode             VARCHAR(10),
    verification_status VARCHAR(30) NOT NULL DEFAULT 'PENDING_VERIFICATION',
    verified_at         TIMESTAMPTZ,
    verified_by         UUID        REFERENCES users(id),
    rejection_reason    TEXT,
    
    -- Audit fields
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by          UUID,
    updated_by          UUID
);

-- ── Organization ↔ Domain Mapping ────────────────────────────
CREATE TABLE IF NOT EXISTS organization_domain_mappings (
    id              UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id UUID        NOT NULL REFERENCES organizations(id) ON DELETE CASCADE,
    domain_id       UUID        NOT NULL REFERENCES domains(id) ON DELETE RESTRICT,
    is_primary      BOOLEAN     NOT NULL DEFAULT FALSE,
    CONSTRAINT uq_org_domain UNIQUE (organization_id, domain_id)
);

-- ── Organization Members ─────────────────────────────────────
CREATE TABLE IF NOT EXISTS organization_members (
    id              UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id UUID        NOT NULL REFERENCES organizations(id) ON DELETE CASCADE,
    user_id         UUID        NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    org_role        VARCHAR(50) NOT NULL,
    designation     VARCHAR(100),
    identifier      VARCHAR(100),
    is_verified     BOOLEAN     NOT NULL DEFAULT FALSE,
    joined_at       TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_org_member UNIQUE (organization_id, user_id)
);

-- ── Government Departments Subsystem ─────────────────────────
CREATE TABLE IF NOT EXISTS departments (
    organization_id      UUID        PRIMARY KEY REFERENCES organizations(id) ON DELETE CASCADE,
    parent_department_id UUID        REFERENCES departments(organization_id),
    level                VARCHAR(30) NOT NULL,
    jurisdiction_area    VARCHAR(255) NOT NULL,
    nodal_officer_name   VARCHAR(100),
    nodal_officer_email  VARCHAR(255),
    nodal_officer_phone  VARCHAR(20)
);

CREATE TABLE IF NOT EXISTS department_problem_categories (
    id                      UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    department_id           UUID        NOT NULL REFERENCES departments(organization_id) ON DELETE CASCADE,
    category_name           VARCHAR(100) NOT NULL,
    description             VARCHAR(255),
    typical_resolution_days INT         DEFAULT 14
);

-- ── Higher Education & Research Subsystem ─────────────────────
CREATE TABLE IF NOT EXISTS university_profiles (
    organization_id        UUID        PRIMARY KEY REFERENCES organizations(id) ON DELETE CASCADE,
    aishe_code             VARCHAR(50) UNIQUE,
    institution_type       VARCHAR(50) NOT NULL,
    naac_grade             VARCHAR(10),
    nirf_rank_range        VARCHAR(50),
    has_incubation_centre  BOOLEAN     NOT NULL DEFAULT FALSE,
    incubation_centre_name VARCHAR(255),
    total_faculty_count    INT         DEFAULT 0,
    total_student_count    INT         DEFAULT 0
);

CREATE TABLE IF NOT EXISTS faculty_profiles (
    id                          UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id                     UUID        NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    organization_id             UUID        NOT NULL REFERENCES organizations(id) ON DELETE CASCADE,
    department_name             VARCHAR(100) NOT NULL,
    designation                 VARCHAR(100) NOT NULL,
    academic_qualification      VARCHAR(100),
    primary_discipline          VARCHAR(100) NOT NULL,
    research_areas              TEXT,
    patents_summary             TEXT,
    publications_count          INT         DEFAULT 0,
    years_of_experience         INT         DEFAULT 0,
    is_available_for_mentorship BOOLEAN     NOT NULL DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS institutional_resources (
    id                             UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id                UUID        NOT NULL REFERENCES organizations(id) ON DELETE CASCADE,
    resource_name                  VARCHAR(255) NOT NULL,
    resource_type                  VARCHAR(50) NOT NULL,
    description                    TEXT,
    equipment_list                 TEXT,
    is_accessible_to_external_teams BOOLEAN     NOT NULL DEFAULT FALSE
);

-- ── Industry & Partner Subsystem ─────────────────────────────
CREATE TABLE IF NOT EXISTS industry_profiles (
    organization_id       UUID           PRIMARY KEY REFERENCES organizations(id) ON DELETE CASCADE,
    registration_number   VARCHAR(100),
    dpiit_recognized      BOOLEAN        DEFAULT FALSE,
    dpiit_number          VARCHAR(50),
    company_stage         VARCHAR(50),
    offering_types        VARCHAR(255),
    annual_csr_budget_inr NUMERIC(15,2),
    focus_sectors         TEXT
);

-- ── Verification Workflow Subsystem ──────────────────────────
CREATE TABLE IF NOT EXISTS verification_requests (
    id                   UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id      UUID        NOT NULL REFERENCES organizations(id) ON DELETE CASCADE,
    status               VARCHAR(30) NOT NULL DEFAULT 'PENDING_VERIFICATION',
    submitted_by         UUID        NOT NULL REFERENCES users(id),
    assigned_reviewer_id UUID        REFERENCES users(id),
    reviewer_notes       TEXT,
    rejection_reason     TEXT,
    submitted_at         TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    resolved_at          TIMESTAMPTZ
);

CREATE TABLE IF NOT EXISTS supporting_documents (
    id                      UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id         UUID        NOT NULL REFERENCES organizations(id) ON DELETE CASCADE,
    verification_request_id UUID        REFERENCES verification_requests(id) ON DELETE CASCADE,
    document_type           VARCHAR(50) NOT NULL,
    document_name           VARCHAR(255) NOT NULL,
    document_url            VARCHAR(500) NOT NULL,
    uploaded_by             UUID        NOT NULL REFERENCES users(id),
    uploaded_at             TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS verification_audit_logs (
    id                      UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id         UUID        NOT NULL REFERENCES organizations(id) ON DELETE CASCADE,
    verification_request_id UUID        REFERENCES verification_requests(id) ON DELETE CASCADE,
    previous_status         VARCHAR(30),
    new_status              VARCHAR(30) NOT NULL,
    action_by               UUID        NOT NULL REFERENCES users(id),
    action_by_role          VARCHAR(50) NOT NULL,
    action_type             VARCHAR(50) NOT NULL,
    comments                TEXT,
    created_at              TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- ================================================================
-- ── Milestone 3: Challenge Crowdsourcing & Lifecycle ────────────
-- ================================================================

CREATE TABLE IF NOT EXISTS challenges (
    id                      UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    tracking_number         VARCHAR(50)     NOT NULL UNIQUE,
    title                   VARCHAR(255)    NOT NULL,
    description             TEXT            NOT NULL,
    
    -- Submitter
    submitted_by            UUID            NOT NULL REFERENCES users(id),
    submitter_type          VARCHAR(50)     NOT NULL,
    
    -- Domain & AI Categorization
    domain_id               UUID            NOT NULL REFERENCES domains(id),
    sub_category            VARCHAR(100),
    ai_predicted_domain_id  UUID            REFERENCES domains(id),
    ai_confidence_score     DECIMAL(4,3),
    ai_keywords             TEXT,
    
    -- Geospatial & Location
    latitude                DECIMAL(10,8)   NOT NULL,
    longitude               DECIMAL(11,8)   NOT NULL,
    address_line            VARCHAR(255),
    locality                VARCHAR(100),
    district                VARCHAR(100)    NOT NULL,
    state                   VARCHAR(100)    NOT NULL,
    pincode                 VARCHAR(10)     NOT NULL,
    jurisdiction_level      VARCHAR(30)     NOT NULL,
    
    -- Priority & Impact Estimation
    severity_level          VARCHAR(20)     NOT NULL,
    urgency_level           VARCHAR(20)     NOT NULL,
    estimated_affected_pop  INT             DEFAULT 0,
    priority_score          DECIMAL(5,2)    DEFAULT 0.00,
    endorsement_count       INT             DEFAULT 0,
    
    -- Deduplication & Clustering
    cluster_id              UUID,
    parent_challenge_id     UUID            REFERENCES challenges(id),
    is_duplicate            BOOLEAN         NOT NULL DEFAULT FALSE,
    duplicate_similarity    DECIMAL(4,3),
    
    -- Lifecycle State & Resolution Path
    status                  VARCHAR(40)     NOT NULL DEFAULT 'SUBMITTED',
    resolution_path         VARCHAR(40)     NOT NULL DEFAULT 'PENDING_TRIAGE',
    
    -- Department Assignment
    assigned_department_id  UUID            REFERENCES departments(organization_id),
    assigned_officer_id     UUID            REFERENCES users(id),
    routing_rationale       TEXT,
    target_resolution_date  TIMESTAMPTZ,
    
    -- Resolution & Impact
    resolved_at             TIMESTAMPTZ,
    resolution_summary      TEXT,
    measurable_impact_desc  TEXT,
    
    -- Audit fields
    created_at              TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    created_by              UUID,
    updated_by              UUID
);

CREATE TABLE IF NOT EXISTS challenge_attachments (
    id              UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    challenge_id    UUID            NOT NULL REFERENCES challenges(id) ON DELETE CASCADE,
    media_type      VARCHAR(30)     NOT NULL,
    file_name       VARCHAR(255)    NOT NULL,
    file_url        VARCHAR(500)    NOT NULL,
    file_size_bytes BIGINT,
    mime_type       VARCHAR(100),
    caption         VARCHAR(255),
    geo_latitude    DECIMAL(10,8),
    geo_longitude   DECIMAL(11,8),
    uploaded_by     UUID            NOT NULL REFERENCES users(id),
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS challenge_endorsements (
    id              UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    challenge_id    UUID            NOT NULL REFERENCES challenges(id) ON DELETE CASCADE,
    user_id         UUID            NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    comment         VARCHAR(500),
    is_affected     BOOLEAN         NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_challenge_user_endorsement UNIQUE (challenge_id, user_id)
);

CREATE TABLE IF NOT EXISTS challenge_department_actions (
    id                      UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    challenge_id            UUID            NOT NULL REFERENCES challenges(id) ON DELETE CASCADE,
    department_id           UUID            REFERENCES departments(organization_id),
    performed_by            UUID            NOT NULL REFERENCES users(id),
    action_type             VARCHAR(50)     NOT NULL,
    field_inspection_notes  TEXT,
    action_notes            TEXT,
    escalation_justification TEXT,
    created_at              TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS challenge_timeline_events (
    id              UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    challenge_id    UUID            NOT NULL REFERENCES challenges(id) ON DELETE CASCADE,
    previous_status VARCHAR(40),
    new_status      VARCHAR(40)     NOT NULL,
    actor_id        UUID            NOT NULL REFERENCES users(id),
    actor_role      VARCHAR(50)     NOT NULL,
    event_title     VARCHAR(150)    NOT NULL,
    event_message   TEXT            NOT NULL,
    is_public       BOOLEAN         NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

-- ── Indexes ──────────────────────────────────────────────────
CREATE INDEX IF NOT EXISTS idx_users_email            ON users(email);
CREATE INDEX IF NOT EXISTS idx_user_roles_user        ON user_roles(user_id);
CREATE INDEX IF NOT EXISTS idx_user_roles_role        ON user_roles(role_id);
CREATE INDEX IF NOT EXISTS idx_org_code               ON organizations(code);
CREATE INDEX IF NOT EXISTS idx_org_status             ON organizations(verification_status);
CREATE INDEX IF NOT EXISTS idx_org_type               ON organizations(organization_type);
CREATE INDEX IF NOT EXISTS idx_org_state_dist         ON organizations(state, district);
CREATE INDEX IF NOT EXISTS idx_org_members_user       ON organization_members(user_id);
CREATE INDEX IF NOT EXISTS idx_org_members_org        ON organization_members(organization_id);
CREATE INDEX IF NOT EXISTS idx_faculty_org            ON faculty_profiles(organization_id);
CREATE INDEX IF NOT EXISTS idx_faculty_user           ON faculty_profiles(user_id);
CREATE INDEX IF NOT EXISTS idx_resources_org          ON institutional_resources(organization_id);
CREATE INDEX IF NOT EXISTS idx_verif_req_org          ON verification_requests(organization_id);
CREATE INDEX IF NOT EXISTS idx_verif_req_status       ON verification_requests(status);
CREATE INDEX IF NOT EXISTS idx_verif_logs_org         ON verification_audit_logs(organization_id);

CREATE INDEX IF NOT EXISTS idx_challenge_tracking     ON challenges(tracking_number);
CREATE INDEX IF NOT EXISTS idx_challenge_status       ON challenges(status);
CREATE INDEX IF NOT EXISTS idx_challenge_domain       ON challenges(domain_id);
CREATE INDEX IF NOT EXISTS idx_challenge_dept         ON challenges(assigned_department_id);
CREATE INDEX IF NOT EXISTS idx_challenge_loc          ON challenges(state, district);
CREATE INDEX IF NOT EXISTS idx_challenge_submitter    ON challenges(submitted_by);
CREATE INDEX IF NOT EXISTS idx_challenge_priority     ON challenges(priority_score DESC);
CREATE INDEX IF NOT EXISTS idx_challenge_cluster      ON challenges(cluster_id);
CREATE INDEX IF NOT EXISTS idx_challenge_attach_ch    ON challenge_attachments(challenge_id);
CREATE INDEX IF NOT EXISTS idx_challenge_endorse_ch   ON challenge_endorsements(challenge_id);
CREATE INDEX IF NOT EXISTS idx_challenge_actions_ch   ON challenge_department_actions(challenge_id);
CREATE INDEX IF NOT EXISTS idx_challenge_timeline_ch  ON challenge_timeline_events(challenge_id);

-- ================================================================
-- ── Milestone 4: Solution Development, Teams & Hackathons ───────
-- ================================================================

CREATE TABLE IF NOT EXISTS teams (
    id                      UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    team_name               VARCHAR(150)    NOT NULL,
    description             TEXT,
    challenge_id            UUID            NOT NULL REFERENCES challenges(id) ON DELETE CASCADE,
    home_university_id      UUID            NOT NULL REFERENCES organizations(id),
    created_by              UUID            NOT NULL REFERENCES users(id),
    status                  VARCHAR(30)     NOT NULL DEFAULT 'FORMING',
    created_at              TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS team_members (
    id                      UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    team_id                 UUID            NOT NULL REFERENCES teams(id) ON DELETE CASCADE,
    user_id                 UUID            NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    university_id           UUID            NOT NULL REFERENCES organizations(id),
    team_role               VARCHAR(40)     NOT NULL, -- TEAM_LEAD, FACULTY_MENTOR, STUDENT, RESEARCHER
    academic_discipline     VARCHAR(100),
    status                  VARCHAR(30)     NOT NULL DEFAULT 'INVITED', -- INVITED, ACTIVE, DECLINED, REMOVED
    invitation_notes        VARCHAR(255),
    joined_at               TIMESTAMPTZ,
    created_at              TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_team_user UNIQUE (team_id, user_id)
);

CREATE TABLE IF NOT EXISTS proposals (
    id                      UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    tracking_number         VARCHAR(50)     NOT NULL UNIQUE, -- e.g., "PRP-2026-08-12345"
    challenge_id            UUID            NOT NULL REFERENCES challenges(id) ON DELETE CASCADE,
    team_id                 UUID            NOT NULL REFERENCES teams(id) ON DELETE CASCADE,
    hackathon_id            UUID,           -- Optional linkage to Hackathon event
    title                   VARCHAR(255)    NOT NULL,
    problem_understanding   TEXT            NOT NULL,
    proposed_solution       TEXT            NOT NULL,
    innovation_novelty      TEXT            NOT NULL,
    technical_approach      TEXT            NOT NULL,
    expected_impact         TEXT            NOT NULL,
    implementation_plan     TEXT            NOT NULL,
    required_resources      TEXT,
    estimated_cost_inr      NUMERIC(15,2),
    scalability_plan        TEXT,
    sustainability_model    TEXT,
    risk_mitigation         TEXT,
    prototype_description   TEXT,
    status                  VARCHAR(40)     NOT NULL DEFAULT 'PROPOSED', -- PROPOSED, UNDER_REVIEW, SHORTLISTED, PROTOTYPING, PILOT_READY, REJECTED
    average_score           DECIMAL(5,2)    DEFAULT 0.00,
    evaluation_count        INT             DEFAULT 0,
    is_shortlisted          BOOLEAN         NOT NULL DEFAULT FALSE,
    rejection_reason        TEXT,
    submitted_by            UUID            NOT NULL REFERENCES users(id),
    submitted_at            TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    created_at              TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS proposal_documents (
    id                      UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    proposal_id             UUID            NOT NULL REFERENCES proposals(id) ON DELETE CASCADE,
    document_type           VARCHAR(50)     NOT NULL, -- TECHNICAL_SPEC, PROTOTYPE_DIAGRAM, RESEARCH_PAPER, BUDGET_SHEET, CAD_MODEL, OTHER
    document_name           VARCHAR(255)    NOT NULL,
    document_url            VARCHAR(500)    NOT NULL,
    uploaded_by             UUID            NOT NULL REFERENCES users(id),
    created_at              TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS proposal_evaluations (
    id                              UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    proposal_id                     UUID            NOT NULL REFERENCES proposals(id) ON DELETE CASCADE,
    evaluator_id                    UUID            NOT NULL REFERENCES users(id),
    problem_understanding_score     INT             NOT NULL, -- 0-100
    innovation_score                INT             NOT NULL, -- 0-100
    technical_feasibility_score     INT             NOT NULL, -- 0-100
    social_impact_score             INT             NOT NULL, -- 0-100
    scalability_score               INT             NOT NULL, -- 0-100
    cost_effectiveness_score        INT             NOT NULL, -- 0-100
    sustainability_score            INT             NOT NULL, -- 0-100
    implementation_readiness_score  INT             NOT NULL, -- 0-100
    total_score                     DECIMAL(5,2)    NOT NULL, -- 0-100
    strengths                       TEXT,
    weaknesses                      TEXT,
    qualitative_feedback            TEXT,
    recommendation                  VARCHAR(30)     NOT NULL, -- SHORTLIST, REVISE, REJECT
    scoring_rationale               TEXT,
    created_at                      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_proposal_evaluator UNIQUE (proposal_id, evaluator_id)
);

CREATE TABLE IF NOT EXISTS proposal_timeline_events (
    id                      UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    proposal_id             UUID            NOT NULL REFERENCES proposals(id) ON DELETE CASCADE,
    previous_status         VARCHAR(40),
    new_status              VARCHAR(40)     NOT NULL,
    actor_id                UUID            NOT NULL REFERENCES users(id),
    actor_role              VARCHAR(50)     NOT NULL,
    event_title             VARCHAR(150)    NOT NULL,
    event_message           TEXT            NOT NULL,
    created_at              TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS hackathons (
    id                      UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    title                   VARCHAR(255)    NOT NULL,
    code                    VARCHAR(100)    NOT NULL UNIQUE,
    description             TEXT            NOT NULL,
    banner_url              VARCHAR(500),
    organizer_org_id        UUID            NOT NULL REFERENCES organizations(id),
    domain_id               UUID            REFERENCES domains(id),
    submission_deadline     TIMESTAMPTZ     NOT NULL,
    evaluation_deadline     TIMESTAMPTZ     NOT NULL,
    status                  VARCHAR(30)     NOT NULL DEFAULT 'UPCOMING', -- DRAFT, UPCOMING, OPEN_FOR_SUBMISSIONS, UNDER_EVALUATION, RESULTS_ANNOUNCED, COMPLETED
    created_by              UUID            NOT NULL REFERENCES users(id),
    created_at              TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS hackathon_challenges (
    id                      UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    hackathon_id            UUID            NOT NULL REFERENCES hackathons(id) ON DELETE CASCADE,
    challenge_id            UUID            NOT NULL REFERENCES challenges(id) ON DELETE CASCADE,
    CONSTRAINT uq_hackathon_challenge UNIQUE (hackathon_id, challenge_id)
);

CREATE TABLE IF NOT EXISTS hackathon_evaluators (
    id                      UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    hackathon_id            UUID            NOT NULL REFERENCES hackathons(id) ON DELETE CASCADE,
    evaluator_id            UUID            NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    specialization_domain   VARCHAR(100),
    assigned_at             TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_hackathon_evaluator UNIQUE (hackathon_id, evaluator_id)
);

CREATE TABLE IF NOT EXISTS project_discussions (
    id                      UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    team_id                 UUID            NOT NULL REFERENCES teams(id) ON DELETE CASCADE,
    proposal_id             UUID            REFERENCES proposals(id) ON DELETE CASCADE,
    sender_id               UUID            NOT NULL REFERENCES users(id),
    message                 TEXT            NOT NULL,
    is_mentor_guidance      BOOLEAN         NOT NULL DEFAULT FALSE,
    attachment_url          VARCHAR(500),
    created_at              TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

-- ── Milestone 4 Indexes ──────────────────────────────────────
CREATE INDEX IF NOT EXISTS idx_teams_challenge        ON teams(challenge_id);
CREATE INDEX IF NOT EXISTS idx_teams_univ             ON teams(home_university_id);
CREATE INDEX IF NOT EXISTS idx_team_members_team      ON team_members(team_id);
CREATE INDEX IF NOT EXISTS idx_team_members_user      ON team_members(user_id);
CREATE INDEX IF NOT EXISTS idx_proposals_ch           ON proposals(challenge_id);
CREATE INDEX IF NOT EXISTS idx_proposals_team         ON proposals(team_id);
CREATE INDEX IF NOT EXISTS idx_proposals_status       ON proposals(status);
CREATE INDEX IF NOT EXISTS idx_proposals_track        ON proposals(tracking_number);
CREATE INDEX IF NOT EXISTS idx_eval_proposal          ON proposal_evaluations(proposal_id);
CREATE INDEX IF NOT EXISTS idx_eval_user              ON proposal_evaluations(evaluator_id);
CREATE INDEX IF NOT EXISTS idx_hackathons_status      ON hackathons(status);
CREATE INDEX IF NOT EXISTS idx_hackathons_code        ON hackathons(code);
CREATE INDEX IF NOT EXISTS idx_disc_team              ON project_discussions(team_id);

-- ================================================================
-- Milestone 5: Industry / CSR / Startup Collaboration, Funding,
-- Mentorship, Co-Development, Testing, Pilot & Impact Tracking
-- ================================================================

-- ── Partner Capabilities ──────────────────────────────────────
CREATE TABLE IF NOT EXISTS partner_capabilities (
    organization_id             UUID            PRIMARY KEY REFERENCES organizations(id) ON DELETE CASCADE,
    sectors                     TEXT,           -- comma-separated or JSON
    technologies                TEXT,
    areas_of_interest           TEXT,
    mentoring_capability        BOOLEAN         NOT NULL DEFAULT FALSE,
    funding_capability          BOOLEAN         NOT NULL DEFAULT FALSE,
    prototyping_capability      BOOLEAN         NOT NULL DEFAULT FALSE,
    testing_capability          BOOLEAN         NOT NULL DEFAULT FALSE,
    deployment_capability       BOOLEAN         NOT NULL DEFAULT FALSE,
    geographic_service_areas    VARCHAR(500),
    available_resources_budget  DECIMAL(15,2)   DEFAULT 0.00,
    created_at                  TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at                  TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

-- ── Collaboration Opportunities ──────────────────────────────
CREATE TABLE IF NOT EXISTS collaboration_opportunities (
    id                          UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    proposal_id                 UUID            NOT NULL REFERENCES proposals(id) ON DELETE CASCADE,
    title                       VARCHAR(255)    NOT NULL,
    description                 TEXT            NOT NULL,
    collaboration_type          VARCHAR(50)     NOT NULL, -- MENTORSHIP, FUNDING, CO_DEVELOPMENT, PROTOTYPING, TESTING, PILOT_IMPLEMENTATION, DEPLOYMENT, TECHNOLOGY_TRANSFER
    skills_sought               TEXT,
    required_resources          TEXT,
    target_sectors              VARCHAR(255),
    is_open                     BOOLEAN         NOT NULL DEFAULT TRUE,
    created_by                  UUID            NOT NULL REFERENCES users(id),
    created_at                  TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at                  TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

-- ── Collaboration Requests ───────────────────────────────────
CREATE TABLE IF NOT EXISTS collaboration_requests (
    id                          UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    opportunity_id              UUID            REFERENCES collaboration_opportunities(id) ON DELETE CASCADE,
    proposal_id                 UUID            NOT NULL REFERENCES proposals(id) ON DELETE CASCADE,
    partner_organization_id     UUID            NOT NULL REFERENCES organizations(id) ON DELETE CASCADE,
    initiated_by_partner        BOOLEAN         NOT NULL DEFAULT FALSE, -- true if partner applied, false if project team requested
    collaboration_type          VARCHAR(50)     NOT NULL,
    status                      VARCHAR(30)     NOT NULL DEFAULT 'REQUESTED', -- REQUESTED, UNDER_REVIEW, ACCEPTED, ACTIVE, COMPLETED, DECLINED, CANCELLED
    message                     TEXT            NOT NULL,
    proposed_contribution       TEXT,
    nominated_contact_person    VARCHAR(150),
    contact_email               VARCHAR(150),
    review_remarks              TEXT,
    created_by                  UUID            NOT NULL REFERENCES users(id),
    created_at                  TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at                  TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

-- ── Mentorship Engagements ───────────────────────────────────
CREATE TABLE IF NOT EXISTS mentorship_engagements (
    id                          UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    proposal_id                 UUID            NOT NULL REFERENCES proposals(id) ON DELETE CASCADE,
    mentor_user_id              UUID            NOT NULL REFERENCES users(id),
    mentor_organization_id      UUID            REFERENCES organizations(id),
    mentorship_status           VARCHAR(30)     NOT NULL DEFAULT 'INVITED', -- INVITED, ACTIVE, COMPLETED, DECLINED
    goals_and_objectives        TEXT,
    invitation_notes            TEXT,
    created_at                  TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at                  TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

-- ── Mentorship Activity Logs ─────────────────────────────────
CREATE TABLE IF NOT EXISTS mentorship_logs (
    id                          UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    engagement_id               UUID            NOT NULL REFERENCES mentorship_engagements(id) ON DELETE CASCADE,
    mentor_user_id              UUID            NOT NULL REFERENCES users(id),
    session_title               VARCHAR(255)    NOT NULL,
    guidance_notes              TEXT            NOT NULL,
    milestones_reviewed         TEXT,
    action_items                TEXT,
    meeting_date                TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    created_at                  TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

-- ── Funding Requirements & Offers ─────────────────────────────
CREATE TABLE IF NOT EXISTS funding_requirements (
    id                          UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    proposal_id                 UUID            NOT NULL REFERENCES proposals(id) ON DELETE CASCADE,
    requested_amount_inr        DECIMAL(15,2)   NOT NULL,
    purpose                     VARCHAR(255)    NOT NULL,
    category                    VARCHAR(50)     NOT NULL, -- EQUIPMENT, PROTOTYPING_MATERIAL, TESTING_FEES, CLOUD_INFRASTRUCTURE, TRAVEL_FIELDWORK, MANPOWER
    justification               TEXT            NOT NULL,
    expected_deliverables       TEXT,
    proposed_timeline           VARCHAR(200),
    is_fulfilled                BOOLEAN         NOT NULL DEFAULT FALSE,
    created_by                  UUID            NOT NULL REFERENCES users(id),
    created_at                  TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at                  TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS funding_offers (
    id                          UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    requirement_id              UUID            NOT NULL REFERENCES funding_requirements(id) ON DELETE CASCADE,
    proposal_id                 UUID            NOT NULL REFERENCES proposals(id) ON DELETE CASCADE,
    sponsor_organization_id     UUID            NOT NULL REFERENCES organizations(id),
    offered_amount_inr          DECIMAL(15,2)   NOT NULL,
    support_type                VARCHAR(50)     NOT NULL, -- MONETARY_GRANT, EQUIPMENT, CLOUD_CREDITS, TESTING_FACILITY_ACCESS, PROTOTYPING_SLOTS, TECHNICAL_MANPOWER
    status                      VARCHAR(30)     NOT NULL DEFAULT 'REQUESTED', -- REQUESTED, UNDER_REVIEW, APPROVED, DISBURSED, UTILIZED, CLOSED
    terms_and_conditions        TEXT,
    disbursed_amount_inr        DECIMAL(15,2)   DEFAULT 0.00,
    disbursed_at                TIMESTAMPTZ,
    utilization_report          TEXT,
    evidence_document_url       VARCHAR(500),
    created_by                  UUID            NOT NULL REFERENCES users(id),
    created_at                  TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at                  TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

-- ── Co-Development Projects ──────────────────────────────────
CREATE TABLE IF NOT EXISTS co_development_projects (
    id                          UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    proposal_id                 UUID            NOT NULL REFERENCES proposals(id) ON DELETE CASCADE,
    partner_organization_id     UUID            NOT NULL REFERENCES organizations(id),
    title                       VARCHAR(255)    NOT NULL,
    objectives                  TEXT            NOT NULL,
    lead_academic_coordinator   VARCHAR(150),
    lead_industry_coordinator   VARCHAR(150),
    start_date                  TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    target_completion_date      TIMESTAMPTZ,
    status                      VARCHAR(30)     NOT NULL DEFAULT 'ACTIVE', -- ACTIVE, ON_HOLD, COMPLETED, TERMINATED
    created_at                  TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at                  TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS co_dev_milestones (
    id                          UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    project_id                  UUID            NOT NULL REFERENCES co_development_projects(id) ON DELETE CASCADE,
    milestone_name              VARCHAR(255)    NOT NULL,
    lead_party                  VARCHAR(50)     NOT NULL, -- ACADEMIC_TEAM, INDUSTRY_PARTNER, JOINT
    deliverables                TEXT            NOT NULL,
    due_date                    TIMESTAMPTZ,
    completion_date             TIMESTAMPTZ,
    status                      VARCHAR(30)     NOT NULL DEFAULT 'PLANNED', -- PLANNED, IN_PROGRESS, COMPLETED, DELAYED
    documentation_url           VARCHAR(500),
    created_at                  TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

-- ── Validation Testing ───────────────────────────────────────
CREATE TABLE IF NOT EXISTS validation_tests (
    id                          UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    proposal_id                 UUID            NOT NULL REFERENCES proposals(id) ON DELETE CASCADE,
    test_type                   VARCHAR(50)     NOT NULL, -- LAB_BENCH_TEST, SAFETY_COMPLIANCE, WATER_QUALITY_ANALYSIS, EFFICIENCY_TEST, DURABILITY_STRESS_TEST, FIELD_SIMULATION
    test_environment            VARCHAR(255)    NOT NULL,
    test_date                   TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    evaluator_name              VARCHAR(150)    NOT NULL,
    parameters_tested           TEXT            NOT NULL,
    test_result                 VARCHAR(30)     NOT NULL, -- PASSED, FAILED, CONDITIONAL_PASS
    issues_identified           TEXT,
    corrective_actions          TEXT,
    evidence_document_url       VARCHAR(500),
    validation_remarks          TEXT,
    created_by                  UUID            NOT NULL REFERENCES users(id),
    created_at                  TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

-- ── Pilot Projects ───────────────────────────────────────────
CREATE TABLE IF NOT EXISTS pilot_projects (
    id                          UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    proposal_id                 UUID            NOT NULL REFERENCES proposals(id) ON DELETE CASCADE,
    pilot_code                  VARCHAR(50)     NOT NULL UNIQUE,
    location_name               VARCHAR(255)    NOT NULL,
    district                    VARCHAR(100)    NOT NULL,
    state                       VARCHAR(100)    NOT NULL,
    pincode                     VARCHAR(10),
    target_population           INT             NOT NULL DEFAULT 0,
    implementation_partner_id   UUID            REFERENCES organizations(id),
    start_date                  TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    expected_end_date           TIMESTAMPTZ,
    actual_end_date             TIMESTAMPTZ,
    status                      VARCHAR(30)     NOT NULL DEFAULT 'PLANNED', -- PLANNED, ACTIVE, PAUSED, COMPLETED, FAILED
    objectives                  TEXT            NOT NULL,
    feedback_notes              TEXT,
    community_validation_status VARCHAR(30)     NOT NULL DEFAULT 'PENDING', -- PENDING, VALIDATED, CONCERNS_RAISED
    created_by                  UUID            NOT NULL REFERENCES users(id),
    created_at                  TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at                  TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

-- ── Impact Measurement & Social KPIs ─────────────────────────
CREATE TABLE IF NOT EXISTS impact_metrics (
    id                          UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    pilot_id                    UUID            REFERENCES pilot_projects(id) ON DELETE CASCADE,
    proposal_id                 UUID            NOT NULL REFERENCES proposals(id) ON DELETE CASCADE,
    kpi_name                    VARCHAR(100)    NOT NULL, -- PEOPLE_BENEFITED, COST_REDUCTION_PERCENT, WATER_SAVED_LITERS_PER_DAY, ENERGY_SAVED_KWH, POLLUTION_REDUCED_PERCENT, TIME_SAVED_HOURS_PER_DAY, VILLAGES_COVERED, HEALTHCARE_OUTCOMES_IMPROVED
    baseline_value              DECIMAL(15,2)   NOT NULL DEFAULT 0.00,
    target_value                DECIMAL(15,2)   NOT NULL DEFAULT 0.00,
    actual_value                DECIMAL(15,2)   NOT NULL DEFAULT 0.00,
    unit_of_measure             VARCHAR(50)     NOT NULL,
    measurement_date            TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    evidence_url                VARCHAR(500),
    verification_status         VARCHAR(30)     NOT NULL DEFAULT 'REPORTED', -- REPORTED, VERIFIED_BY_GOVERNMENT, DISPUTED
    verified_by_user_id         UUID            REFERENCES users(id),
    verified_at                 TIMESTAMPTZ,
    remarks                     TEXT,
    created_at                  TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

-- ── Technology Transfer ──────────────────────────────────────
CREATE TABLE IF NOT EXISTS tech_transfer_records (
    id                          UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    proposal_id                 UUID            NOT NULL REFERENCES proposals(id) ON DELETE CASCADE,
    asset_name                  VARCHAR(255)    NOT NULL,
    ip_registration_number      VARCHAR(100),
    licensing_type              VARCHAR(50)     NOT NULL, -- NON_EXCLUSIVE, EXCLUSIVE, OPEN_SOURCE_GOV, ROYALTY_FREE_PUBLIC
    receiving_organization_id   UUID            NOT NULL REFERENCES organizations(id),
    transfer_date               TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    responsible_parties         TEXT            NOT NULL,
    deployment_status           VARCHAR(50)     NOT NULL DEFAULT 'TRANSFERRED', -- TRANSFERRED, COMMERCIALIZED, PUBLIC_DEPLOYMENT_ACTIVE
    documentation_url           VARCHAR(500),
    created_by                  UUID            NOT NULL REFERENCES users(id),
    created_at                  TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

-- ── Milestone 5 Indexes ──────────────────────────────────────
CREATE INDEX IF NOT EXISTS idx_collab_opp_prop        ON collaboration_opportunities(proposal_id);
CREATE INDEX IF NOT EXISTS idx_collab_req_prop        ON collaboration_requests(proposal_id);
CREATE INDEX IF NOT EXISTS idx_collab_req_org         ON collaboration_requests(partner_organization_id);
CREATE INDEX IF NOT EXISTS idx_collab_req_status      ON collaboration_requests(status);
CREATE INDEX IF NOT EXISTS idx_mentor_prop            ON mentorship_engagements(proposal_id);
CREATE INDEX IF NOT EXISTS idx_mentor_user            ON mentorship_engagements(mentor_user_id);
CREATE INDEX IF NOT EXISTS idx_funding_req_prop       ON funding_requirements(proposal_id);
CREATE INDEX IF NOT EXISTS idx_funding_offer_req      ON funding_offers(requirement_id);
CREATE INDEX IF NOT EXISTS idx_funding_offer_org      ON funding_offers(sponsor_organization_id);
CREATE INDEX IF NOT EXISTS idx_codev_prop             ON co_development_projects(proposal_id);
CREATE INDEX IF NOT EXISTS idx_codev_org              ON co_development_projects(partner_organization_id);
CREATE INDEX IF NOT EXISTS idx_val_test_prop          ON validation_tests(proposal_id);
CREATE INDEX IF NOT EXISTS idx_pilot_prop             ON pilot_projects(proposal_id);
CREATE INDEX IF NOT EXISTS idx_pilot_code             ON pilot_projects(pilot_code);
CREATE INDEX IF NOT EXISTS idx_pilot_status           ON pilot_projects(status);
CREATE INDEX IF NOT EXISTS idx_impact_prop            ON impact_metrics(proposal_id);
CREATE INDEX IF NOT EXISTS idx_impact_pilot           ON impact_metrics(pilot_id);
CREATE INDEX IF NOT EXISTS idx_tech_transfer_prop     ON tech_transfer_records(proposal_id);

-- ── Push Notifications & Device Tokens ───────────────────────
CREATE TABLE IF NOT EXISTS device_tokens (
    id                          UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id                     UUID            NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token                       VARCHAR(500)    NOT NULL UNIQUE,
    device_type                 VARCHAR(50)     NOT NULL DEFAULT 'ANDROID', -- ANDROID, IOS, WEB
    last_active_at              TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    created_at                  TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS notifications (
    id                          UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id                     UUID            NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    title                       VARCHAR(255)    NOT NULL,
    body                        TEXT            NOT NULL,
    notification_type           VARCHAR(50)     NOT NULL, -- CHALLENGE_SUBMITTED, CHALLENGE_ROUTED, CHALLENGE_TRIAGED, INNOVATION_REQUIRED, CHALLENGE_RESOLVED, ENDORSEMENT_RECEIVED, GENERAL
    reference_id                VARCHAR(100),
    reference_type              VARCHAR(50),
    is_read                     BOOLEAN         NOT NULL DEFAULT FALSE,
    read_at                     TIMESTAMPTZ,
    created_at                  TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_device_tokens_user     ON device_tokens(user_id);
CREATE INDEX IF NOT EXISTS idx_device_tokens_token    ON device_tokens(token);
CREATE INDEX IF NOT EXISTS idx_notifications_user     ON notifications(user_id);
CREATE INDEX IF NOT EXISTS idx_notifications_unread   ON notifications(user_id, is_read);



