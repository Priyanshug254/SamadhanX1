-- ============================================================
-- SamadhanX: 004_storage_policies.sql
-- Storage Buckets & Policies for Supabase Storage
-- ============================================================

-- Create default storage buckets if not existing
INSERT INTO storage.buckets (id, name, public, file_size_limit, allowed_mime_types)
VALUES 
    ('challenge-media', 'challenge-media', true, 52428800, ARRAY['image/jpeg', 'image/png', 'image/webp', 'image/gif', 'video/mp4']),
    ('challenge-documents', 'challenge-documents', true, 52428800, ARRAY['application/pdf', 'application/msword', 'application/vnd.openxmlformats-officedocument.wordprocessingml.document', 'text/plain']),
    ('organization-documents', 'organization-documents', false, 52428800, ARRAY['application/pdf', 'image/jpeg', 'image/png']),
    ('solution-documents', 'solution-documents', false, 52428800, ARRAY['application/pdf', 'application/zip', 'image/jpeg', 'image/png']),
    ('pilot-evidence', 'pilot-evidence', true, 52428800, ARRAY['image/jpeg', 'image/png', 'application/pdf', 'video/mp4']),
    ('avatars', 'avatars', true, 10485760, ARRAY['image/jpeg', 'image/png', 'image/webp'])
ON CONFLICT (id) DO UPDATE SET
    public = EXCLUDED.public,
    file_size_limit = EXCLUDED.file_size_limit,
    allowed_mime_types = EXCLUDED.allowed_mime_types;

-- ============================================================
-- STORAGE POLICIES: challenge-media
-- ============================================================
CREATE POLICY "Public read for challenge-media"
    ON storage.objects FOR SELECT
    TO public
    USING (bucket_id = 'challenge-media');

CREATE POLICY "Authenticated users can upload challenge-media"
    ON storage.objects FOR INSERT
    TO authenticated
    WITH CHECK (bucket_id = 'challenge-media');

-- ============================================================
-- STORAGE POLICIES: challenge-documents
-- ============================================================
CREATE POLICY "Public read for challenge-documents"
    ON storage.objects FOR SELECT
    TO public
    USING (bucket_id = 'challenge-documents');

CREATE POLICY "Authenticated users can upload challenge-documents"
    ON storage.objects FOR INSERT
    TO authenticated
    WITH CHECK (bucket_id = 'challenge-documents');

-- ============================================================
-- STORAGE POLICIES: organization-documents (Private)
-- ============================================================
CREATE POLICY "Authenticated users can upload organization-documents"
    ON storage.objects FOR INSERT
    TO authenticated
    WITH CHECK (bucket_id = 'organization-documents');

CREATE POLICY "Org members and Admins can view organization-documents"
    ON storage.objects FOR SELECT
    TO authenticated
    USING (
        bucket_id = 'organization-documents'
        AND (
            public.is_admin(auth.uid())
            OR (storage.foldername(name))[1] = auth.uid()::text
        )
    );

-- ============================================================
-- STORAGE POLICIES: avatars
-- ============================================================
CREATE POLICY "Public read for avatars"
    ON storage.objects FOR SELECT
    TO public
    USING (bucket_id = 'avatars');

CREATE POLICY "Authenticated users can upload own avatar"
    ON storage.objects FOR INSERT
    TO authenticated
    WITH CHECK (
        bucket_id = 'avatars'
        AND (storage.foldername(name))[1] = auth.uid()::text
    );
