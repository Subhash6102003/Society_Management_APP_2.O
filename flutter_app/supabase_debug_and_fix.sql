-- ============================================================
-- MGB Heights — Supabase Diagnostic & Fix Script
-- Run each SECTION one at a time in the SQL Editor.
-- Each section is clearly marked. Read the output carefully.
-- ============================================================

-- ============================================================
-- SECTION 1: CHECK IF ANY USERS EXIST IN THE USERS TABLE
-- Expected: You should see rows for every signup attempt.
-- If this is EMPTY after a signup, the trigger is broken.
-- ============================================================
SELECT
  id,
  email,
  name,
  role,
  approval_status,
  is_profile_complete,
  is_onboarded,
  created_at
FROM public.users
ORDER BY created_at DESC
LIMIT 20;

-- ============================================================
-- SECTION 2: CHECK IF THE TRIGGER EXISTS AND IS ATTACHED
-- Expected: You should see "on_auth_user_created" in the results.
-- If MISSING, the trigger was never created — run Section 6.
-- ============================================================
SELECT
  trigger_name,
  event_manipulation,
  event_object_table,
  action_statement,
  action_timing
FROM information_schema.triggers
WHERE trigger_schema = 'auth'
  AND event_object_table = 'users';

-- ============================================================
-- SECTION 3: CHECK THE TRIGGER FUNCTION BODY
-- Expected: Should read name and role from raw_user_meta_data.
-- If it says just "INSERT INTO public.users (id, email)" with
-- no name/role from metadata, run Section 6 to fix it.
-- ============================================================
SELECT prosrc
FROM pg_proc
WHERE proname = 'handle_new_auth_user';

-- ============================================================
-- SECTION 4: CHECK PENDING USERS (what admin should see)
-- Expected: You should see users who signed up but aren't approved.
-- If empty after a signup, either the trigger failed or
-- the approval_status is not 'pending'.
-- ============================================================
SELECT
  id,
  email,
  name,
  role,
  approval_status,
  is_profile_complete,
  phone_number,
  flat_number,
  tower_block
FROM public.users
WHERE approval_status = 'pending'
ORDER BY created_at DESC;

-- ============================================================
-- SECTION 5: CHECK get_my_role() FUNCTION EXISTS
-- Expected: Should return the function definition.
-- ============================================================
SELECT routine_name, routine_definition
FROM information_schema.routines
WHERE routine_name = 'get_my_role';

-- ============================================================
-- SECTION 6: FIX — Replace the trigger function
-- Run this to fix the trigger so it properly reads name & role
-- from the metadata Flutter sends during signUp().
-- SAFE TO RUN: Uses CREATE OR REPLACE.
-- ============================================================
CREATE OR REPLACE FUNCTION handle_new_auth_user()
RETURNS TRIGGER AS $$
BEGIN
  INSERT INTO public.users (
    id,
    email,
    name,
    role,
    approval_status,
    is_profile_complete,
    is_onboarded,
    is_blocked,
    phone_number,
    profile_photo_url,
    id_proof_url,
    flat_number,
    tower_block,
    house_number,
    created_at,
    updated_at
  )
  VALUES (
    NEW.id,
    NEW.email,
    COALESCE(NEW.raw_user_meta_data->>'name', ''),
    COALESCE((NEW.raw_user_meta_data->>'role')::user_role, 'resident'),
    'pending',
    FALSE,
    FALSE,
    FALSE,
    '',
    '',
    '',
    '',
    '',
    '',
    EXTRACT(EPOCH FROM NOW())::BIGINT * 1000,
    EXTRACT(EPOCH FROM NOW())::BIGINT * 1000
  )
  ON CONFLICT (id) DO UPDATE
    SET
      name        = COALESCE(NEW.raw_user_meta_data->>'name', EXCLUDED.name),
      role        = COALESCE((NEW.raw_user_meta_data->>'role')::user_role, EXCLUDED.role),
      updated_at  = EXTRACT(EPOCH FROM NOW())::BIGINT * 1000;
  RETURN NEW;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- Re-attach trigger (DROP first to avoid duplicate)
DROP TRIGGER IF EXISTS on_auth_user_created ON auth.users;
CREATE TRIGGER on_auth_user_created
  AFTER INSERT ON auth.users
  FOR EACH ROW
  EXECUTE FUNCTION handle_new_auth_user();

-- ============================================================
-- SECTION 7: FIX — Add an RPC function for profile completion
-- This bypasses RLS issues during the profile completion step.
-- The app will call this function instead of a direct UPDATE.
-- SAFE TO RUN: Uses CREATE OR REPLACE.
-- ============================================================
CREATE OR REPLACE FUNCTION complete_user_profile(
  p_user_id         UUID,
  p_name            TEXT,
  p_phone_number    TEXT,
  p_flat_number     TEXT,
  p_tower_block     TEXT,
  p_profile_photo_url TEXT,
  p_id_proof_url    TEXT
)
RETURNS SETOF public.users AS $$
BEGIN
  -- Only allow the user to complete their own profile
  IF auth.uid() != p_user_id THEN
    RAISE EXCEPTION 'Unauthorized: can only complete your own profile';
  END IF;

  RETURN QUERY
  UPDATE public.users
  SET
    name                = p_name,
    phone_number        = p_phone_number,
    flat_number         = p_flat_number,
    tower_block         = p_tower_block,
    profile_photo_url   = p_profile_photo_url,
    id_proof_url        = p_id_proof_url,
    is_profile_complete = TRUE,
    is_onboarded        = TRUE,
    approval_status     = 'pending',
    updated_at          = EXTRACT(EPOCH FROM NOW())::BIGINT * 1000
  WHERE id = p_user_id
  RETURNING *;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- ============================================================
-- SECTION 8: FIX — Add an RPC function for admin approval
-- Allows admin to approve/reject a user by ID.
-- ============================================================
CREATE OR REPLACE FUNCTION admin_set_user_approval(
  p_user_id UUID,
  p_status  TEXT  -- 'approved' or 'rejected'
)
RETURNS SETOF public.users AS $$
BEGIN
  -- Only admins can call this
  IF get_my_role() != 'admin' THEN
    RAISE EXCEPTION 'Unauthorized: admin only';
  END IF;

  RETURN QUERY
  UPDATE public.users
  SET
    approval_status = p_status::approval_status,
    updated_at      = EXTRACT(EPOCH FROM NOW())::BIGINT * 1000
  WHERE id = p_user_id
  RETURNING *;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- ============================================================
-- SECTION 9: VERIFY — Check Realtime is enabled for users table
-- Run this AFTER enabling Realtime in Dashboard.
-- Dashboard → Database → Replication → turn ON for "users" table
-- ============================================================
SELECT schemaname, tablename
FROM pg_publication_tables
WHERE pubname = 'supabase_realtime';

-- ============================================================
-- SECTION 10: MANUAL TEST — Insert a test user to verify trigger
-- Temporarily disable email auth to test DB-level trigger only.
-- DO NOT RUN in production with real users — for testing only.
-- ============================================================
-- (Skip this section unless you're debugging trigger issues)
-- SELECT * FROM public.users WHERE email = 'test@mgbheights.com';
-- DELETE FROM auth.users WHERE email = 'test@mgbheights.com';

-- ============================================================
-- INSTRUCTIONS:
-- 1. Run Section 1 first — check if users exist in the table
-- 2. Run Section 2 — verify trigger is attached
-- 3. Run Section 3 — verify trigger reads metadata
-- 4. Run Section 6 — this FIXES the trigger (safe to run always)
-- 5. Run Section 7 — adds the completeProfile RPC function
-- 6. Run Section 8 — adds the admin approval RPC function
-- 7. Run Section 9 — check if Realtime is enabled
--
-- IMPORTANT SETTING:
-- Go to: Supabase Dashboard → Authentication → Providers → Email
-- Turn OFF "Confirm email" (email verification for new signups)
-- This is the #1 cause of signup failures in dev.
-- ============================================================
