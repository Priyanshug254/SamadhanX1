-- ============================================================
-- SamadhanX: 001_initial_schema.sql
-- Societal Challenge Crowdsourcing & Problem-Solving Ecosystem
-- ============================================================

-- Enable required extensions
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- 1. Roles table
CREATE TABLE IF NOT EXISTS public.roles (
    name VARCHAR(50) PRIMARY KEY,
    description TEXT NOT NULL,
    is_self_registerable BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMPTZ NOT NULL DEFAULT timezone('utc'::text, now())
);

-- 2. Profiles table (associated with auth.users)
CREATE TABLE IF NOT EXISTS public.profiles (
    id UUID PRIMARY KEY REFERENCES auth.users(id) ON DELETE CASCADE,
    email VARCHAR(255) UNIQUE NOT NULL,
    first_name VARCHAR(100) NOT NULL DEFAULT '',
    last_name VARCHAR(100) NOT NULL DEFAULT '',
    phone_number VARCHAR(20),
    avatar_url TEXT,
    is_active BOOLEAN NOT NULL DEFAULT true,
    is_email_verified BOOLEAN NOT NULL DEFAULT false,
    organization_id UUID,
    created_at TIMESTAMPTZ NOT NULL DEFAULT timezone('utc'::text, now()),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT timezone('utc'::text, now())
);

-- 3. User Roles mapping
CREATE TABLE IF NOT EXISTS public.user_roles (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES public.profiles(id) ON DELETE CASCADE,
    role_name VARCHAR(50) NOT NULL REFERENCES public.roles(name) ON DELETE RESTRICT,
    assigned_by UUID REFERENCES public.profiles(id),
    created_at TIMESTAMPTZ NOT NULL DEFAULT timezone('utc'::text, now()),
    CONSTRAINT uq_user_role UNIQUE (user_id, role_name)
);

-- 4. Organizations table
CREATE TABLE IF NOT EXISTS public.organizations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    code VARCHAR(50) UNIQUE NOT NULL,
    type VARCHAR(50) NOT NULL, -- GOVERNMENT_DEPARTMENT, UNIVERSITY, INDUSTRY, STARTUP, MSME, CSR, RESEARCH_LAB, INNOVATION_HUB, COMMUNITY_ORG
    description TEXT,
    contact_email VARCHAR(255) NOT NULL,
    contact_phone VARCHAR(20),
    website TEXT,
    registration_number VARCHAR(100),
    address TEXT,
    district VARCHAR(100),
    state VARCHAR(100),
    country VARCHAR(100) DEFAULT 'India',
    verification_status VARCHAR(50) NOT NULL DEFAULT 'PENDING_VERIFICATION', -- PENDING_VERIFICATION, UNDER_REVIEW, VERIFIED, REJECTED, SUSPENDED
    created_by UUID REFERENCES public.profiles(id),
    created_at TIMESTAMPTZ NOT NULL DEFAULT timezone('utc'::text, now()),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT timezone('utc'::text, now())
);

-- Link profiles.organization_id to organizations(id)
ALTER TABLE public.profiles 
    ADD CONSTRAINT fk_profile_org FOREIGN KEY (organization_id) REFERENCES public.organizations(id) ON DELETE SET NULL;

-- 5. Organization Members
CREATE TABLE IF NOT EXISTS public.organization_members (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id UUID NOT NULL REFERENCES public.organizations(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES public.profiles(id) ON DELETE CASCADE,
    member_role VARCHAR(50) NOT NULL DEFAULT 'MEMBER', -- ADMIN, MEMBER, MENTOR, RESEARCHER, LEAD
    is_primary_contact BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMPTZ NOT NULL DEFAULT timezone('utc'::text, now()),
    CONSTRAINT uq_org_member UNIQUE (organization_id, user_id)
);

-- 6. Organization Verifications (Audit Trail)
CREATE TABLE IF NOT EXISTS public.organization_verifications (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id UUID NOT NULL REFERENCES public.organizations(id) ON DELETE CASCADE,
    reviewer_id UUID NOT NULL REFERENCES public.profiles(id),
    decision VARCHAR(50) NOT NULL, -- VERIFIED, REJECTED, SUSPENDED, UNDER_REVIEW
    reason TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT timezone('utc'::text, now())
);

-- 7. Organization Documents
CREATE TABLE IF NOT EXISTS public.organization_documents (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id UUID NOT NULL REFERENCES public.organizations(id) ON DELETE CASCADE,
    title VARCHAR(255) NOT NULL,
    document_type VARCHAR(50) NOT NULL, -- REGISTRATION_CERT, TAX_ID, ACCREDITATION, AUTHORIZATION_LETTER, OTHER
    file_url TEXT NOT NULL,
    uploaded_at TIMESTAMPTZ NOT NULL DEFAULT timezone('utc'::text, now())
);

-- 8. Domains (Societal Problem Domains)
CREATE TABLE IF NOT EXISTS public.domains (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code VARCHAR(50) UNIQUE NOT NULL,
    name VARCHAR(150) NOT NULL,
    description TEXT,
    icon_name VARCHAR(50) DEFAULT 'domain',
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMPTZ NOT NULL DEFAULT timezone('utc'::text, now())
);

-- 9. Challenges
CREATE TABLE IF NOT EXISTS public.challenges (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tracking_number VARCHAR(50) UNIQUE NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    domain_id UUID NOT NULL REFERENCES public.domains(id) ON DELETE RESTRICT,
    problem_category VARCHAR(100),
    location TEXT NOT NULL,
    latitude DOUBLE PRECISION,
    longitude DOUBLE PRECISION,
    address TEXT,
    district VARCHAR(100),
    state VARCHAR(100),
    country VARCHAR(100) DEFAULT 'India',
    severity VARCHAR(50) NOT NULL DEFAULT 'MEDIUM', -- LOW, MEDIUM, HIGH, CRITICAL
    urgency VARCHAR(50) NOT NULL DEFAULT 'MEDIUM', -- LOW, MEDIUM, HIGH, IMMEDIATE
    affected_population VARCHAR(100),
    submitter_id UUID NOT NULL REFERENCES public.profiles(id),
    status VARCHAR(50) NOT NULL DEFAULT 'SUBMITTED', -- DRAFT, SUBMITTED, UNDER_REVIEW, VERIFIED, ROUTED, ACCEPTED, IN_COLLABORATION, SOLUTION_PROPOSED, UNDER_EVALUATION, PROTOTYPE, PILOT, IMPLEMENTATION, RESOLVED, CLOSED, REJECTED
    endorsement_count INTEGER NOT NULL DEFAULT 0,
    assigned_department_id UUID REFERENCES public.organizations(id),
    ai_summary TEXT,
    ai_category_tags JSONB DEFAULT '[]'::jsonb,
    created_at TIMESTAMPTZ NOT NULL DEFAULT timezone('utc'::text, now()),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT timezone('utc'::text, now())
);

-- 10. Challenge Media & Documents
CREATE TABLE IF NOT EXISTS public.challenge_media (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    challenge_id UUID NOT NULL REFERENCES public.challenges(id) ON DELETE CASCADE,
    media_url TEXT NOT NULL,
    media_type VARCHAR(50) NOT NULL DEFAULT 'IMAGE', -- IMAGE, VIDEO, AUDIO
    file_name VARCHAR(255),
    created_at TIMESTAMPTZ NOT NULL DEFAULT timezone('utc'::text, now())
);

CREATE TABLE IF NOT EXISTS public.challenge_documents (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    challenge_id UUID NOT NULL REFERENCES public.challenges(id) ON DELETE CASCADE,
    document_url TEXT NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    file_size BIGINT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT timezone('utc'::text, now())
);

-- 11. Challenge Timeline & History
CREATE TABLE IF NOT EXISTS public.challenge_timeline (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    challenge_id UUID NOT NULL REFERENCES public.challenges(id) ON DELETE CASCADE,
    status VARCHAR(50) NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    actor_id UUID REFERENCES public.profiles(id),
    metadata JSONB DEFAULT '{}'::jsonb,
    created_at TIMESTAMPTZ NOT NULL DEFAULT timezone('utc'::text, now())
);

-- 12. Challenge Endorsements (Citizen support)
CREATE TABLE IF NOT EXISTS public.challenge_endorsements (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    challenge_id UUID NOT NULL REFERENCES public.challenges(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES public.profiles(id) ON DELETE CASCADE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT timezone('utc'::text, now()),
    CONSTRAINT uq_challenge_endorsement UNIQUE (challenge_id, user_id)
);

-- 13. Challenge Assignments (Gov routing)
CREATE TABLE IF NOT EXISTS public.challenge_assignments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    challenge_id UUID NOT NULL REFERENCES public.challenges(id) ON DELETE CASCADE,
    department_id UUID NOT NULL REFERENCES public.organizations(id) ON DELETE CASCADE,
    assigned_by UUID REFERENCES public.profiles(id),
    assigned_to_user_id UUID REFERENCES public.profiles(id),
    status VARCHAR(50) NOT NULL DEFAULT 'ASSIGNED', -- ASSIGNED, ACCEPTED, REASSIGNED, COMPLETED
    notes TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT timezone('utc'::text, now())
);

-- 14. Teams (Universities / Startups / Innovation Hubs)
CREATE TABLE IF NOT EXISTS public.teams (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    organization_id UUID REFERENCES public.organizations(id) ON DELETE SET NULL,
    leader_id UUID NOT NULL REFERENCES public.profiles(id),
    challenge_id UUID REFERENCES public.challenges(id) ON DELETE SET NULL,
    faculty_mentor_id UUID REFERENCES public.profiles(id),
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE', -- ACTIVE, DISBANDED, COMPLETED
    created_at TIMESTAMPTZ NOT NULL DEFAULT timezone('utc'::text, now()),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT timezone('utc'::text, now())
);

CREATE TABLE IF NOT EXISTS public.team_members (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    team_id UUID NOT NULL REFERENCES public.teams(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES public.profiles(id) ON DELETE CASCADE,
    role VARCHAR(50) NOT NULL DEFAULT 'MEMBER', -- LEADER, CO_LEADER, DEVELOPER, RESEARCHER, MENTOR
    joined_at TIMESTAMPTZ NOT NULL DEFAULT timezone('utc'::text, now()),
    CONSTRAINT uq_team_member UNIQUE (team_id, user_id)
);

-- 15. Solution Proposals
CREATE TABLE IF NOT EXISTS public.solution_proposals (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    challenge_id UUID NOT NULL REFERENCES public.challenges(id) ON DELETE CASCADE,
    team_id UUID REFERENCES public.teams(id) ON DELETE SET NULL,
    proposer_id UUID NOT NULL REFERENCES public.profiles(id),
    organization_id UUID REFERENCES public.organizations(id),
    title VARCHAR(255) NOT NULL,
    problem_understanding TEXT NOT NULL,
    proposed_solution TEXT NOT NULL,
    technology_stack TEXT,
    implementation_plan TEXT,
    estimated_budget NUMERIC(15, 2),
    timeline_weeks INTEGER,
    expected_impact TEXT,
    prototype_url TEXT,
    status VARCHAR(50) NOT NULL DEFAULT 'SUBMITTED', -- SUBMITTED, UNDER_EVALUATION, SHORTLISTED, ACCEPTED, REJECTED, WITHDRAWN
    score NUMERIC(5, 2),
    reviewer_feedback TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT timezone('utc'::text, now()),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT timezone('utc'::text, now())
);

CREATE TABLE IF NOT EXISTS public.solution_attachments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    proposal_id UUID NOT NULL REFERENCES public.solution_proposals(id) ON DELETE CASCADE,
    file_name VARCHAR(255) NOT NULL,
    file_url TEXT NOT NULL,
    file_type VARCHAR(50) NOT NULL DEFAULT 'DOCUMENT',
    created_at TIMESTAMPTZ NOT NULL DEFAULT timezone('utc'::text, now())
);

-- 16. Partner Capabilities & Matching
CREATE TABLE IF NOT EXISTS public.partner_capabilities (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id UUID NOT NULL REFERENCES public.organizations(id) ON DELETE CASCADE,
    capability_type VARCHAR(100) NOT NULL, -- TECHNOLOGY, MANUFACTURING, DEPLOYMENT, FUNDING, MENTORSHIP, INFRASTRUCTURE
    domain_id UUID REFERENCES public.domains(id) ON DELETE SET NULL,
    technology_tags TEXT[] DEFAULT ARRAY[]::TEXT[],
    description TEXT,
    capacity VARCHAR(100),
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMPTZ NOT NULL DEFAULT timezone('utc'::text, now()),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT timezone('utc'::text, now())
);

CREATE TABLE IF NOT EXISTS public.partner_matches (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    challenge_id UUID NOT NULL REFERENCES public.challenges(id) ON DELETE CASCADE,
    organization_id UUID NOT NULL REFERENCES public.organizations(id) ON DELETE CASCADE,
    match_score NUMERIC(5, 2) NOT NULL DEFAULT 0.0,
    match_reasons JSONB DEFAULT '[]'::jsonb,
    status VARCHAR(50) NOT NULL DEFAULT 'RECOMMENDED', -- RECOMMENDED, CONTACTED, ENGAGED, DECLINED
    contacted_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT timezone('utc'::text, now()),
    CONSTRAINT uq_partner_match UNIQUE (challenge_id, organization_id)
);

-- 17. CSR Funding & Commitments
CREATE TABLE IF NOT EXISTS public.funding_opportunities (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    challenge_id UUID REFERENCES public.challenges(id) ON DELETE SET NULL,
    organization_id UUID NOT NULL REFERENCES public.organizations(id) ON DELETE CASCADE,
    title VARCHAR(255) NOT NULL,
    budget_amount NUMERIC(15, 2) NOT NULL,
    currency VARCHAR(10) DEFAULT 'INR',
    grant_terms TEXT,
    deadline TIMESTAMPTZ,
    status VARCHAR(50) NOT NULL DEFAULT 'OPEN', -- OPEN, ALLOCATED, CLOSED
    created_at TIMESTAMPTZ NOT NULL DEFAULT timezone('utc'::text, now())
);

CREATE TABLE IF NOT EXISTS public.funding_commitments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    funding_opportunity_id UUID REFERENCES public.funding_opportunities(id) ON DELETE CASCADE,
    proposal_id UUID REFERENCES public.solution_proposals(id) ON DELETE CASCADE,
    organization_id UUID NOT NULL REFERENCES public.organizations(id),
    amount_committed NUMERIC(15, 2) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'PLEDGED', -- PLEDGED, DISBURSED, COMPLETED, CANCELLED
    terms TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT timezone('utc'::text, now())
);

-- 18. Pilot Deployments & Impact Tracking
CREATE TABLE IF NOT EXISTS public.pilot_deployments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    challenge_id UUID NOT NULL REFERENCES public.challenges(id) ON DELETE CASCADE,
    proposal_id UUID REFERENCES public.solution_proposals(id) ON DELETE SET NULL,
    lead_organization_id UUID NOT NULL REFERENCES public.organizations(id),
    title VARCHAR(255) NOT NULL,
    deployment_location TEXT NOT NULL,
    latitude DOUBLE PRECISION,
    longitude DOUBLE PRECISION,
    start_date DATE,
    end_date DATE,
    status VARCHAR(50) NOT NULL DEFAULT 'PLANNED', -- PLANNED, ACTIVE, COMPLETED, FAILED, PAUSED
    target_population INTEGER,
    budget NUMERIC(15, 2),
    created_at TIMESTAMPTZ NOT NULL DEFAULT timezone('utc'::text, now()),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT timezone('utc'::text, now())
);

CREATE TABLE IF NOT EXISTS public.pilot_metrics (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    pilot_id UUID NOT NULL REFERENCES public.pilot_deployments(id) ON DELETE CASCADE,
    metric_name VARCHAR(150) NOT NULL,
    target_value NUMERIC(15, 2) NOT NULL,
    current_value NUMERIC(15, 2) NOT NULL DEFAULT 0.0,
    unit VARCHAR(50) NOT NULL,
    last_measured_at TIMESTAMPTZ DEFAULT timezone('utc'::text, now())
);

CREATE TABLE IF NOT EXISTS public.pilot_evidence (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    pilot_id UUID NOT NULL REFERENCES public.pilot_deployments(id) ON DELETE CASCADE,
    title VARCHAR(255) NOT NULL,
    file_url TEXT NOT NULL,
    evidence_type VARCHAR(50) NOT NULL DEFAULT 'PHOTO', -- PHOTO, REPORT, DATASET, VIDEO
    description TEXT,
    recorded_at TIMESTAMPTZ NOT NULL DEFAULT timezone('utc'::text, now())
);

-- 19. Notifications & Device Tokens
CREATE TABLE IF NOT EXISTS public.notifications (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES public.profiles(id) ON DELETE CASCADE,
    type VARCHAR(50) NOT NULL DEFAULT 'SYSTEM', -- CHALLENGE_UPDATE, PROPOSAL_UPDATE, ASSIGNMENT, VERIFICATION, MENTION
    title VARCHAR(255) NOT NULL,
    message TEXT NOT NULL,
    reference_type VARCHAR(50),
    reference_id UUID,
    is_read BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMPTZ NOT NULL DEFAULT timezone('utc'::text, now())
);

CREATE TABLE IF NOT EXISTS public.device_tokens (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES public.profiles(id) ON DELETE CASCADE,
    token TEXT NOT NULL,
    platform VARCHAR(20) NOT NULL DEFAULT 'android', -- android, ios, web
    last_seen_at TIMESTAMPTZ NOT NULL DEFAULT timezone('utc'::text, now()),
    CONSTRAINT uq_user_device_token UNIQUE (user_id, token)
);

-- 20. Audit Logs
CREATE TABLE IF NOT EXISTS public.audit_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    actor_id UUID REFERENCES public.profiles(id),
    action VARCHAR(100) NOT NULL,
    entity VARCHAR(100) NOT NULL,
    entity_id VARCHAR(100),
    metadata JSONB DEFAULT '{}'::jsonb,
    created_at TIMESTAMPTZ NOT NULL DEFAULT timezone('utc'::text, now())
);

-- Indexes for high performance querying
CREATE INDEX IF NOT EXISTS idx_challenges_tracking_number ON public.challenges(tracking_number);
CREATE INDEX IF NOT EXISTS idx_challenges_domain_id ON public.challenges(domain_id);
CREATE INDEX IF NOT EXISTS idx_challenges_submitter_id ON public.challenges(submitter_id);
CREATE INDEX IF NOT EXISTS idx_challenges_status ON public.challenges(status);
CREATE INDEX IF NOT EXISTS idx_challenges_location ON public.challenges(district, state);
CREATE INDEX IF NOT EXISTS idx_timeline_challenge_id ON public.challenge_timeline(challenge_id);
CREATE INDEX IF NOT EXISTS idx_endorsements_challenge_id ON public.challenge_endorsements(challenge_id);
CREATE INDEX IF NOT EXISTS idx_endorsements_user_id ON public.challenge_endorsements(user_id);
CREATE INDEX IF NOT EXISTS idx_proposals_challenge_id ON public.solution_proposals(challenge_id);
CREATE INDEX IF NOT EXISTS idx_organizations_verification ON public.organizations(verification_status);
CREATE INDEX IF NOT EXISTS idx_notifications_user_id ON public.notifications(user_id, is_read);
CREATE INDEX IF NOT EXISTS idx_audit_logs_actor ON public.audit_logs(actor_id, action);
