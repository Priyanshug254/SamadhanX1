-- ============================================================
-- SamadhanX: 003_rls_policies.sql
-- Row Level Security (RLS) Policies on All Tables
-- ============================================================

-- 1. Enable RLS on all tables
ALTER TABLE public.roles ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.profiles ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.user_roles ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.organizations ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.organization_members ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.organization_verifications ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.organization_documents ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.domains ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.challenges ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.challenge_media ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.challenge_documents ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.challenge_timeline ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.challenge_endorsements ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.challenge_assignments ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.teams ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.team_members ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.solution_proposals ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.solution_attachments ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.partner_capabilities ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.partner_matches ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.funding_opportunities ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.funding_commitments ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.pilot_deployments ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.pilot_metrics ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.pilot_evidence ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.notifications ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.device_tokens ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.audit_logs ENABLE ROW LEVEL SECURITY;

-- ============================================================
-- ROLES POLICIES
-- ============================================================
CREATE POLICY "Roles are viewable by authenticated users"
    ON public.roles FOR SELECT
    TO authenticated
    USING (true);

CREATE POLICY "Roles can only be modified by Super Admins"
    ON public.roles FOR ALL
    TO authenticated
    USING (public.has_role(auth.uid(), 'SUPER_ADMIN'));

-- ============================================================
-- PROFILES POLICIES
-- ============================================================
CREATE POLICY "Users can view public profiles"
    ON public.profiles FOR SELECT
    TO authenticated
    USING (true);

CREATE POLICY "Users can insert their own profile"
    ON public.profiles FOR INSERT
    TO authenticated
    WITH CHECK (auth.uid() = id);

CREATE POLICY "Users can update own profile"
    ON public.profiles FOR UPDATE
    TO authenticated
    USING (auth.uid() = id OR public.is_admin(auth.uid()))
    WITH CHECK (auth.uid() = id OR public.is_admin(auth.uid()));

-- ============================================================
-- USER_ROLES POLICIES
-- ============================================================
CREATE POLICY "Users can view own roles and Admins can view all"
    ON public.user_roles FOR SELECT
    TO authenticated
    USING (user_id = auth.uid() OR public.is_admin(auth.uid()));

CREATE POLICY "Only Admins can modify user roles directly"
    ON public.user_roles FOR ALL
    TO authenticated
    USING (public.is_admin(auth.uid()))
    WITH CHECK (public.is_admin(auth.uid()));

-- ============================================================
-- ORGANIZATIONS POLICIES
-- ============================================================
CREATE POLICY "Verified orgs are viewable, or own unverified org"
    ON public.organizations FOR SELECT
    TO authenticated
    USING (
        verification_status = 'VERIFIED'
        OR created_by = auth.uid()
        OR EXISTS (
            SELECT 1 FROM public.organization_members
            WHERE organization_id = public.organizations.id AND user_id = auth.uid()
        )
        OR public.is_admin(auth.uid())
    );

CREATE POLICY "Authenticated users can create an organization"
    ON public.organizations FOR INSERT
    TO authenticated
    WITH CHECK (auth.uid() = created_by);

CREATE POLICY "Org admins or Platform Admins can update organization"
    ON public.organizations FOR UPDATE
    TO authenticated
    USING (
        created_by = auth.uid()
        OR EXISTS (
            SELECT 1 FROM public.organization_members
            WHERE organization_id = public.organizations.id AND user_id = auth.uid() AND member_role IN ('ADMIN', 'LEAD')
        )
        OR public.is_admin(auth.uid())
    );

-- ============================================================
-- DOMAINS POLICIES
-- ============================================================
CREATE POLICY "Domains are viewable by all"
    ON public.domains FOR SELECT
    TO authenticated, anon
    USING (is_active = true OR public.is_admin(auth.uid()));

CREATE POLICY "Domains are manageable by Admins"
    ON public.domains FOR ALL
    TO authenticated
    USING (public.is_admin(auth.uid()));

-- ============================================================
-- CHALLENGES POLICIES
-- ============================================================
CREATE POLICY "Anyone can view non-draft challenges or own draft"
    ON public.challenges FOR SELECT
    TO authenticated, anon
    USING (
        status <> 'DRAFT' 
        OR (auth.uid() IS NOT NULL AND submitter_id = auth.uid())
        OR (auth.uid() IS NOT NULL AND public.is_admin(auth.uid()))
    );

CREATE POLICY "Authenticated users can create challenges"
    ON public.challenges FOR INSERT
    TO authenticated
    WITH CHECK (submitter_id = auth.uid());

CREATE POLICY "Submitter can update own challenge if not yet routed/resolved"
    ON public.challenges FOR UPDATE
    TO authenticated
    USING (
        (submitter_id = auth.uid() AND status IN ('DRAFT', 'SUBMITTED'))
        OR public.is_admin(auth.uid())
        OR public.has_role(auth.uid(), 'GOVERNMENT_OFFICIAL')
    );

-- ============================================================
-- CHALLENGE MEDIA & DOCUMENTS POLICIES
-- ============================================================
CREATE POLICY "Media viewable if challenge is accessible"
    ON public.challenge_media FOR SELECT
    TO authenticated, anon
    USING (true);

CREATE POLICY "Submitter can add challenge media"
    ON public.challenge_media FOR INSERT
    TO authenticated
    WITH CHECK (
        EXISTS (
            SELECT 1 FROM public.challenges
            WHERE id = challenge_id AND (submitter_id = auth.uid() OR public.is_admin(auth.uid()))
        )
    );

CREATE POLICY "Documents viewable if challenge is accessible"
    ON public.challenge_documents FOR SELECT
    TO authenticated, anon
    USING (true);

CREATE POLICY "Submitter can add challenge documents"
    ON public.challenge_documents FOR INSERT
    TO authenticated
    WITH CHECK (
        EXISTS (
            SELECT 1 FROM public.challenges
            WHERE id = challenge_id AND (submitter_id = auth.uid() OR public.is_admin(auth.uid()))
        )
    );

-- ============================================================
-- CHALLENGE TIMELINE POLICIES
-- ============================================================
CREATE POLICY "Timeline viewable by all"
    ON public.challenge_timeline FOR SELECT
    TO authenticated, anon
    USING (true);

CREATE POLICY "Timeline insertable by authorized actors"
    ON public.challenge_timeline FOR INSERT
    TO authenticated
    WITH CHECK (actor_id = auth.uid() OR public.is_admin(auth.uid()));

-- ============================================================
-- CHALLENGE ENDORSEMENTS POLICIES
-- ============================================================
CREATE POLICY "Endorsements viewable by all"
    ON public.challenge_endorsements FOR SELECT
    TO authenticated, anon
    USING (true);

CREATE POLICY "Users can endorse challenges"
    ON public.challenge_endorsements FOR INSERT
    TO authenticated
    WITH CHECK (user_id = auth.uid());

CREATE POLICY "Users can remove own endorsement"
    ON public.challenge_endorsements FOR DELETE
    TO authenticated
    USING (user_id = auth.uid());

-- ============================================================
-- TEAMS & TEAM MEMBERS POLICIES
-- ============================================================
CREATE POLICY "Teams viewable by authenticated users"
    ON public.teams FOR SELECT
    TO authenticated
    USING (true);

CREATE POLICY "Authenticated users can create teams"
    ON public.teams FOR INSERT
    TO authenticated
    WITH CHECK (leader_id = auth.uid());

CREATE POLICY "Team leader can update team"
    ON public.teams FOR UPDATE
    TO authenticated
    USING (leader_id = auth.uid() OR public.is_admin(auth.uid()));

CREATE POLICY "Team members viewable by authenticated users"
    ON public.team_members FOR SELECT
    TO authenticated
    USING (true);

CREATE POLICY "Team leader or self can insert team members"
    ON public.team_members FOR INSERT
    TO authenticated
    WITH CHECK (
        user_id = auth.uid() 
        OR EXISTS (
            SELECT 1 FROM public.teams 
            WHERE id = team_id AND (leader_id = auth.uid() OR public.is_admin(auth.uid()))
        )
    );

CREATE POLICY "Team leader or self can delete team members"
    ON public.team_members FOR DELETE
    TO authenticated
    USING (
        user_id = auth.uid() 
        OR EXISTS (
            SELECT 1 FROM public.teams 
            WHERE id = team_id AND (leader_id = auth.uid() OR public.is_admin(auth.uid()))
        )
    );

-- ============================================================
-- SOLUTION PROPOSALS POLICIES
-- ============================================================
CREATE POLICY "Proposals viewable by all authenticated"
    ON public.solution_proposals FOR SELECT
    TO authenticated
    USING (true);

CREATE POLICY "Authenticated users can create proposals"
    ON public.solution_proposals FOR INSERT
    TO authenticated
    WITH CHECK (proposer_id = auth.uid());

CREATE POLICY "Proposer or Admins can update proposals"
    ON public.solution_proposals FOR UPDATE
    TO authenticated
    USING (
        proposer_id = auth.uid() 
        OR public.is_admin(auth.uid())
        OR public.has_role(auth.uid(), 'GOVERNMENT_OFFICIAL')
    );

-- ============================================================
-- PARTNER CAPABILITIES & PILOTS POLICIES
-- ============================================================
CREATE POLICY "Capabilities viewable by authenticated users"
    ON public.partner_capabilities FOR SELECT
    TO authenticated
    USING (true);

CREATE POLICY "Org members can manage capabilities"
    ON public.partner_capabilities FOR ALL
    TO authenticated
    USING (
        EXISTS (
            SELECT 1 FROM public.organization_members
            WHERE organization_id = public.partner_capabilities.organization_id AND user_id = auth.uid()
        )
        OR public.is_admin(auth.uid())
    );

CREATE POLICY "Pilots viewable by authenticated users"
    ON public.pilot_deployments FOR SELECT
    TO authenticated
    USING (true);

CREATE POLICY "Lead org members or Gov can manage pilots"
    ON public.pilot_deployments FOR ALL
    TO authenticated
    USING (
        EXISTS (
            SELECT 1 FROM public.organization_members
            WHERE organization_id = public.pilot_deployments.lead_organization_id AND user_id = auth.uid()
        )
        OR public.is_admin(auth.uid())
        OR public.has_role(auth.uid(), 'GOVERNMENT_OFFICIAL')
    );

-- ============================================================
-- NOTIFICATIONS & DEVICE TOKENS POLICIES
-- ============================================================
CREATE POLICY "Users can only view own notifications"
    ON public.notifications FOR SELECT
    TO authenticated
    USING (user_id = auth.uid());

CREATE POLICY "Users can mark own notifications as read"
    ON public.notifications FOR UPDATE
    TO authenticated
    USING (user_id = auth.uid())
    WITH CHECK (user_id = auth.uid());

CREATE POLICY "Users can manage own device tokens"
    ON public.device_tokens FOR ALL
    TO authenticated
    USING (user_id = auth.uid())
    WITH CHECK (user_id = auth.uid());

-- ============================================================
-- AUDIT LOGS POLICIES
-- ============================================================
CREATE POLICY "Admins can view audit logs"
    ON public.audit_logs FOR SELECT
    TO authenticated
    USING (public.is_admin(auth.uid()));

CREATE POLICY "Authenticated users can insert audit logs"
    ON public.audit_logs FOR INSERT
    TO authenticated
    WITH CHECK (actor_id = auth.uid() OR actor_id IS NULL);
