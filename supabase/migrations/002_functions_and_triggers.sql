-- ============================================================
-- SamadhanX: 002_functions_and_triggers.sql
-- Database Functions, RPCs, and Automated Triggers
-- ============================================================

-- Function: Get all role names for a user
CREATE OR REPLACE FUNCTION public.get_user_roles(p_user_id UUID)
RETURNS VARCHAR[] AS $$
DECLARE
    roles_list VARCHAR[];
BEGIN
    SELECT COALESCE(array_agg(role_name), ARRAY[]::VARCHAR[])
    INTO roles_list
    FROM public.user_roles
    WHERE user_id = p_user_id;
    
    RETURN roles_list;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- Function: Check if user has specific role
CREATE OR REPLACE FUNCTION public.has_role(p_user_id UUID, p_role_name VARCHAR)
RETURNS BOOLEAN AS $$
BEGIN
    RETURN EXISTS (
        SELECT 1 FROM public.user_roles
        WHERE user_id = p_user_id AND role_name = p_role_name
    );
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- Function: Check if user is Super Admin or Government Admin
CREATE OR REPLACE FUNCTION public.is_admin(p_user_id UUID)
RETURNS BOOLEAN AS $$
BEGIN
    RETURN EXISTS (
        SELECT 1 FROM public.user_roles
        WHERE user_id = p_user_id AND role_name IN ('SUPER_ADMIN', 'GOVERNMENT_ADMIN')
    );
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- Function: Check if user is a verified organization member
CREATE OR REPLACE FUNCTION public.is_verified_user(p_user_id UUID)
RETURNS BOOLEAN AS $$
BEGIN
    RETURN EXISTS (
        SELECT 1 FROM public.profiles p
        LEFT JOIN public.organizations o ON p.organization_id = o.id
        WHERE p.id = p_user_id 
          AND (p.is_email_verified = true OR (o.id IS NOT NULL AND o.verification_status = 'VERIFIED'))
    );
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- Trigger Function: Auto-create profile and assign role upon signup in auth.users
CREATE OR REPLACE FUNCTION public.handle_new_user()
RETURNS TRIGGER AS $$
DECLARE
    v_raw_role VARCHAR(50);
    v_target_role VARCHAR(50);
    v_first_name VARCHAR(100);
    v_last_name VARCHAR(100);
    v_phone VARCHAR(20);
    v_is_self_reg BOOLEAN;
BEGIN
    -- Extract user metadata passed during supabase.auth.signUp()
    v_raw_role := COALESCE(NEW.raw_user_meta_data->>'role', 'CITIZEN');
    v_first_name := COALESCE(NEW.raw_user_meta_data->>'first_name', '');
    v_last_name := COALESCE(NEW.raw_user_meta_data->>'last_name', '');
    v_phone := NEW.raw_user_meta_data->>'phone_number';

    -- Check if requested role is self-registerable
    SELECT is_self_registerable INTO v_is_self_reg
    FROM public.roles
    WHERE name = v_raw_role;

    IF v_is_self_reg IS TRUE THEN
        v_target_role := v_raw_role;
    ELSE
        -- Fallback to CITIZEN for safety if a privileged role was requested anonymously
        v_target_role := 'CITIZEN';
    END IF;

    -- Insert into public.profiles
    INSERT INTO public.profiles (
        id,
        email,
        first_name,
        last_name,
        phone_number,
        is_active,
        is_email_verified
    ) VALUES (
        NEW.id,
        NEW.email,
        v_first_name,
        v_last_name,
        v_phone,
        true,
        COALESCE(NEW.email_confirmed_at IS NOT NULL, false)
    ) ON CONFLICT (id) DO UPDATE SET
        email = EXCLUDED.email,
        first_name = CASE WHEN EXCLUDED.first_name <> '' THEN EXCLUDED.first_name ELSE public.profiles.first_name END,
        last_name = CASE WHEN EXCLUDED.last_name <> '' THEN EXCLUDED.last_name ELSE public.profiles.last_name END,
        is_email_verified = EXCLUDED.is_email_verified,
        updated_at = timezone('utc'::text, now());

    -- Assign role in user_roles
    INSERT INTO public.user_roles (user_id, role_name)
    VALUES (NEW.id, v_target_role)
    ON CONFLICT (user_id, role_name) DO NOTHING;

    -- Audit log
    INSERT INTO public.audit_logs (actor_id, action, entity, entity_id, metadata)
    VALUES (NEW.id, 'USER_REGISTERED', 'profiles', NEW.id::text, jsonb_build_object('role', v_target_role, 'email', NEW.email));

    RETURN NEW;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- Attach trigger to auth.users
DROP TRIGGER IF EXISTS on_auth_user_created ON auth.users;
CREATE TRIGGER on_auth_user_created
    AFTER INSERT ON auth.users
    FOR EACH ROW EXECUTE FUNCTION public.handle_new_user();

-- Trigger to sync email confirmation status
CREATE OR REPLACE FUNCTION public.handle_user_email_confirmed()
RETURNS TRIGGER AS $$
BEGIN
    IF NEW.email_confirmed_at IS NOT NULL AND OLD.email_confirmed_at IS NULL THEN
        UPDATE public.profiles
        SET is_email_verified = true,
            updated_at = timezone('utc'::text, now())
        WHERE id = NEW.id;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

DROP TRIGGER IF EXISTS on_auth_user_email_confirmed ON auth.users;
CREATE TRIGGER on_auth_user_email_confirmed
    AFTER UPDATE OF email_confirmed_at ON auth.users
    FOR EACH ROW EXECUTE FUNCTION public.handle_user_email_confirmed();

-- Helper: Generate collision-safe human-friendly tracking number
CREATE OR REPLACE FUNCTION public.generate_tracking_number()
RETURNS VARCHAR(50) AS $$
DECLARE
    v_year TEXT := to_char(CURRENT_DATE, 'YYYY');
    v_random_code TEXT;
    v_tracking_num TEXT;
    v_exists BOOLEAN;
BEGIN
    LOOP
        -- Format: SAM-YYYY-XXXXXX (6 alphanumeric uppercase chars)
        v_random_code := UPPER(SUBSTRING(MD5(gen_random_uuid()::TEXT) FROM 1 FOR 6));
        v_tracking_num := 'SAM-' || v_year || '-' || v_random_code;

        SELECT EXISTS (SELECT 1 FROM public.challenges WHERE tracking_number = v_tracking_num) INTO v_exists;
        IF NOT v_exists THEN
            RETURN v_tracking_num;
        END IF;
    END LOOP;
END;
$$ LANGUAGE plpgsql;

-- RPC: Atomic Submit Challenge
CREATE OR REPLACE FUNCTION public.create_challenge(
    p_title VARCHAR(255),
    p_description TEXT,
    p_domain_id UUID,
    p_location TEXT,
    p_latitude DOUBLE PRECISION DEFAULT NULL,
    p_longitude DOUBLE PRECISION DEFAULT NULL,
    p_address TEXT DEFAULT NULL,
    p_district VARCHAR(100) DEFAULT NULL,
    p_state VARCHAR(100) DEFAULT NULL,
    p_country VARCHAR(100) DEFAULT 'India',
    p_severity VARCHAR(50) DEFAULT 'MEDIUM',
    p_urgency VARCHAR(50) DEFAULT 'MEDIUM',
    p_affected_population VARCHAR(100) DEFAULT NULL,
    p_problem_category VARCHAR(100) DEFAULT NULL,
    p_media_urls TEXT[] DEFAULT ARRAY[]::TEXT[]
)
RETURNS JSONB AS $$
DECLARE
    v_user_id UUID;
    v_tracking_no VARCHAR(50);
    v_challenge_id UUID;
    v_media_url TEXT;
    v_result JSONB;
BEGIN
    v_user_id := auth.uid();
    IF v_user_id IS NULL THEN
        RAISE EXCEPTION 'Unauthorized: User must be authenticated to submit a challenge';
    END IF;

    -- Generate tracking number
    v_tracking_no := public.generate_tracking_number();

    -- Insert challenge record
    INSERT INTO public.challenges (
        tracking_number,
        title,
        description,
        domain_id,
        problem_category,
        location,
        latitude,
        longitude,
        address,
        district,
        state,
        country,
        severity,
        urgency,
        affected_population,
        submitter_id,
        status
    ) VALUES (
        v_tracking_no,
        p_title,
        p_description,
        p_domain_id,
        p_problem_category,
        p_location,
        p_latitude,
        p_longitude,
        p_address,
        p_district,
        p_state,
        p_country,
        p_severity,
        p_urgency,
        p_affected_population,
        v_user_id,
        'SUBMITTED'
    ) RETURNING id INTO v_challenge_id;

    -- Insert media records if provided
    IF p_media_urls IS NOT NULL AND array_length(p_media_urls, 1) > 0 THEN
        FOREACH v_media_url IN ARRAY p_media_urls LOOP
            INSERT INTO public.challenge_media (challenge_id, media_url, media_type)
            VALUES (v_challenge_id, v_media_url, 'IMAGE');
        END LOOP;
    END IF;

    -- Create initial timeline event
    INSERT INTO public.challenge_timeline (
        challenge_id,
        status,
        title,
        description,
        actor_id,
        metadata
    ) VALUES (
        v_challenge_id,
        'SUBMITTED',
        'Challenge Submitted',
        'Problem reported by citizen. Tracking number generated: ' || v_tracking_no,
        v_user_id,
        jsonb_build_object('tracking_number', v_tracking_no, 'location', p_location)
    );

    -- Create in-app notification for the user
    INSERT INTO public.notifications (
        user_id,
        type,
        title,
        message,
        reference_type,
        reference_id
    ) VALUES (
        v_user_id,
        'CHALLENGE_SUBMITTED',
        'Challenge Submitted Successfully',
        'Your challenge "' || p_title || '" has been registered with tracking ID ' || v_tracking_no,
        'CHALLENGE',
        v_challenge_id
    );

    -- Audit log
    INSERT INTO public.audit_logs (actor_id, action, entity, entity_id, metadata)
    VALUES (v_user_id, 'CHALLENGE_CREATED', 'challenges', v_challenge_id::text, jsonb_build_object('tracking_number', v_tracking_no));

    -- Construct return object
    SELECT to_jsonb(c.*) INTO v_result
    FROM public.challenges c
    WHERE c.id = v_challenge_id;

    RETURN v_result;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- RPC: Atomic Endorse Challenge
CREATE OR REPLACE FUNCTION public.endorse_challenge(p_challenge_id UUID)
RETURNS JSONB AS $$
DECLARE
    v_user_id UUID;
    v_new_count INTEGER;
BEGIN
    v_user_id := auth.uid();
    IF v_user_id IS NULL THEN
        RAISE EXCEPTION 'Unauthorized: User must be authenticated to endorse a challenge';
    END IF;

    -- Insert endorsement (enforces uniqueness via UNIQUE constraint)
    INSERT INTO public.challenge_endorsements (challenge_id, user_id)
    VALUES (p_challenge_id, v_user_id)
    ON CONFLICT (challenge_id, user_id) DO NOTHING;

    -- Update endorsement count
    UPDATE public.challenges
    SET endorsement_count = (SELECT count(*) FROM public.challenge_endorsements WHERE challenge_id = p_challenge_id),
        updated_at = timezone('utc'::text, now())
    WHERE id = p_challenge_id
    RETURNING endorsement_count INTO v_new_count;

    RETURN jsonb_build_object('challenge_id', p_challenge_id, 'endorsement_count', v_new_count, 'endorsed', true);
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- RPC: Atomic Un-endorse Challenge
CREATE OR REPLACE FUNCTION public.unendorse_challenge(p_challenge_id UUID)
RETURNS JSONB AS $$
DECLARE
    v_user_id UUID;
    v_new_count INTEGER;
BEGIN
    v_user_id := auth.uid();
    IF v_user_id IS NULL THEN
        RAISE EXCEPTION 'Unauthorized: User must be authenticated to unendorse';
    END IF;

    DELETE FROM public.challenge_endorsements
    WHERE challenge_id = p_challenge_id AND user_id = v_user_id;

    UPDATE public.challenges
    SET endorsement_count = (SELECT count(*) FROM public.challenge_endorsements WHERE challenge_id = p_challenge_id),
        updated_at = timezone('utc'::text, now())
    WHERE id = p_challenge_id
    RETURNING endorsement_count INTO v_new_count;

    RETURN jsonb_build_object('challenge_id', p_challenge_id, 'endorsement_count', v_new_count, 'endorsed', false);
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- RPC: Update Challenge Status & Append Timeline
CREATE OR REPLACE FUNCTION public.change_challenge_status(
    p_challenge_id UUID,
    p_new_status VARCHAR(50),
    p_title VARCHAR(255),
    p_description TEXT DEFAULT NULL,
    p_assigned_department_id UUID DEFAULT NULL
)
RETURNS JSONB AS $$
DECLARE
    v_user_id UUID;
    v_submitter_id UUID;
    v_challenge_title VARCHAR(255);
    v_old_status VARCHAR(50);
BEGIN
    v_user_id := auth.uid();
    IF v_user_id IS NULL THEN
        RAISE EXCEPTION 'Unauthorized';
    END IF;

    -- Fetch current state
    SELECT submitter_id, title, status INTO v_submitter_id, v_challenge_title, v_old_status
    FROM public.challenges
    WHERE id = p_challenge_id;

    IF v_submitter_id IS NULL THEN
        RAISE EXCEPTION 'Challenge not found';
    END IF;

    -- Update status
    UPDATE public.challenges
    SET status = p_new_status,
        assigned_department_id = COALESCE(p_assigned_department_id, assigned_department_id),
        updated_at = timezone('utc'::text, now())
    WHERE id = p_challenge_id;

    -- Append timeline event
    INSERT INTO public.challenge_timeline (
        challenge_id,
        status,
        title,
        description,
        actor_id,
        metadata
    ) VALUES (
        p_challenge_id,
        p_new_status,
        p_title,
        p_description,
        v_user_id,
        jsonb_build_object('old_status', v_old_status, 'new_status', p_new_status)
    );

    -- Notify submitter if actor is not the submitter
    IF v_user_id <> v_submitter_id THEN
        INSERT INTO public.notifications (
            user_id,
            type,
            title,
            message,
            reference_type,
            reference_id
        ) VALUES (
            v_submitter_id,
            'CHALLENGE_STATUS_UPDATE',
            'Status Update: ' || p_title,
            'Your challenge "' || v_challenge_title || '" has moved to status ' || p_new_status || '.',
            'CHALLENGE',
            p_challenge_id
        );
    END IF;

    -- Audit log
    INSERT INTO public.audit_logs (actor_id, action, entity, entity_id, metadata)
    VALUES (v_user_id, 'STATUS_CHANGE', 'challenges', p_challenge_id::text, jsonb_build_object('from', v_old_status, 'to', p_new_status));

    RETURN jsonb_build_object('success', true, 'challenge_id', p_challenge_id, 'status', p_new_status);
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- RPC: Verify Organization (Super Admin / Gov Admin)
CREATE OR REPLACE FUNCTION public.verify_organization(
    p_org_id UUID,
    p_decision VARCHAR(50), -- VERIFIED, REJECTED, SUSPENDED, UNDER_REVIEW
    p_reason TEXT
)
RETURNS JSONB AS $$
DECLARE
    v_user_id UUID;
    v_org_creator UUID;
    v_org_name VARCHAR(255);
BEGIN
    v_user_id := auth.uid();
    IF v_user_id IS NULL OR NOT public.is_admin(v_user_id) THEN
        RAISE EXCEPTION 'Unauthorized: Only platform administrators can verify organizations';
    END IF;

    IF p_decision = 'REJECTED' AND (p_reason IS NULL OR TRIM(p_reason) = '') THEN
        RAISE EXCEPTION 'A valid rejection reason is mandatory';
    END IF;

    SELECT created_by, name INTO v_org_creator, v_org_name
    FROM public.organizations
    WHERE id = p_org_id;

    IF v_org_name IS NULL THEN
        RAISE EXCEPTION 'Organization not found';
    END IF;

    -- Update organization status
    UPDATE public.organizations
    SET verification_status = p_decision,
        updated_at = timezone('utc'::text, now())
    WHERE id = p_org_id;

    -- Insert verification audit record
    INSERT INTO public.organization_verifications (
        organization_id,
        reviewer_id,
        decision,
        reason
    ) VALUES (
        p_org_id,
        v_user_id,
        p_decision,
        p_reason
    );

    -- Notify organization creator
    IF v_org_creator IS NOT NULL THEN
        INSERT INTO public.notifications (
            user_id,
            type,
            title,
            message,
            reference_type,
            reference_id
        ) VALUES (
            v_org_creator,
            'ORGANIZATION_VERIFICATION',
            'Organization Status Update: ' || v_org_name,
            'Your organization verification status is now ' || p_decision || '. Note: ' || p_reason,
            'ORGANIZATION',
            p_org_id
        );
    END IF;

    -- Audit log
    INSERT INTO public.audit_logs (actor_id, action, entity, entity_id, metadata)
    VALUES (v_user_id, 'VERIFY_ORGANIZATION', 'organizations', p_org_id::text, jsonb_build_object('decision', p_decision, 'reason', p_reason));

    RETURN jsonb_build_object('success', true, 'organization_id', p_org_id, 'verification_status', p_decision);
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- RPC: Ecosystem Stats Aggregator
CREATE OR REPLACE FUNCTION public.get_ecosystem_stats()
RETURNS JSONB AS $$
DECLARE
    v_total_users BIGINT;
    v_total_citizens BIGINT;
    v_total_orgs BIGINT;
    v_verified_orgs BIGINT;
    v_total_challenges BIGINT;
    v_resolved_challenges BIGINT;
    v_in_progress_challenges BIGINT;
    v_total_proposals BIGINT;
    v_active_pilots BIGINT;
BEGIN
    SELECT count(*) INTO v_total_users FROM public.profiles;
    
    SELECT count(DISTINCT user_id) INTO v_total_citizens 
    FROM public.user_roles WHERE role_name = 'CITIZEN';

    SELECT count(*) INTO v_total_orgs FROM public.organizations;
    SELECT count(*) INTO v_verified_orgs FROM public.organizations WHERE verification_status = 'VERIFIED';
    
    SELECT count(*) INTO v_total_challenges FROM public.challenges;
    SELECT count(*) INTO v_resolved_challenges FROM public.challenges WHERE status IN ('RESOLVED', 'CLOSED');
    SELECT count(*) INTO v_in_progress_challenges FROM public.challenges WHERE status IN ('IN_COLLABORATION', 'SOLUTION_PROPOSED', 'UNDER_EVALUATION', 'PROTOTYPE', 'PILOT', 'IMPLEMENTATION');
    
    SELECT count(*) INTO v_total_proposals FROM public.solution_proposals;
    SELECT count(*) INTO v_active_pilots FROM public.pilot_deployments WHERE status = 'ACTIVE';

    RETURN jsonb_build_object(
        'total_users', v_total_users,
        'total_citizens', v_total_citizens,
        'total_organizations', v_total_orgs,
        'verified_organizations', v_verified_orgs,
        'total_challenges', v_total_challenges,
        'resolved_challenges', v_resolved_challenges,
        'in_progress_challenges', v_in_progress_challenges,
        'total_proposals', v_total_proposals,
        'active_pilots', v_active_pilots
    );
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;
