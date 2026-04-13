-- ============================================================
-- MIGRATION: Add shop enhancements + fix auth trigger
-- Run this on an EXISTING database that already has the base schema.
-- Safe to run multiple times (uses IF NOT EXISTS / OR REPLACE).
-- ============================================================

-- ── 1. Add missing columns to shop_listings ───────────────────────────────────
ALTER TABLE shop_listings
  ADD COLUMN IF NOT EXISTS is_free    BOOLEAN NOT NULL DEFAULT FALSE,
  ADD COLUMN IF NOT EXISTS is_sold    BOOLEAN NOT NULL DEFAULT FALSE,
  ADD COLUMN IF NOT EXISTS expires_at BIGINT  NOT NULL DEFAULT 0;

-- Backfill expires_at for any existing rows (set to 15 days from created_at)
UPDATE shop_listings
SET expires_at = created_at + (15 * 24 * 60 * 60 * 1000)
WHERE expires_at = 0;

-- Fix default category from '' to 'other' for any blank categories
UPDATE shop_listings SET category = 'other' WHERE category = '';

-- ── 2. Fix auth trigger to read name + role from signup metadata ──────────────
CREATE OR REPLACE FUNCTION handle_new_auth_user()
RETURNS TRIGGER AS $$
BEGIN
  INSERT INTO public.users (id, email, name, role, created_at, updated_at)
  VALUES (
    NEW.id,
    NEW.email,
    COALESCE(NEW.raw_user_meta_data->>'name', ''),
    COALESCE((NEW.raw_user_meta_data->>'role')::user_role, 'resident'),
    EXTRACT(EPOCH FROM NOW())::BIGINT * 1000,
    EXTRACT(EPOCH FROM NOW())::BIGINT * 1000
  )
  ON CONFLICT (id) DO NOTHING;
  RETURN NEW;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- Re-attach trigger (safe — replaces if exists)
DROP TRIGGER IF EXISTS on_auth_user_created ON auth.users;
CREATE TRIGGER on_auth_user_created
  AFTER INSERT ON auth.users
  FOR EACH ROW
  EXECUTE FUNCTION handle_new_auth_user();

-- ── 3. Auto-delete expired shop listings via pg_cron ─────────────────────────
-- NOTE: pg_cron must be enabled in your Supabase project.
-- Enable via: Dashboard → Database → Extensions → pg_cron
-- Then run the SELECT below to register the daily cleanup job.

-- Uncomment after enabling pg_cron:
-- SELECT cron.schedule(
--   'delete-expired-shop-listings',
--   '0 2 * * *',   -- runs daily at 02:00 UTC
--   $$
--     DELETE FROM shop_listings
--     WHERE is_sold = FALSE
--       AND is_available = TRUE
--       AND expires_at > 0
--       AND expires_at < (EXTRACT(EPOCH FROM NOW())::BIGINT * 1000);
--   $$
-- );

-- ── 4. Verify ─────────────────────────────────────────────────────────────────
-- Run this to confirm columns were added:
-- SELECT column_name, data_type, column_default
-- FROM information_schema.columns
-- WHERE table_name = 'shop_listings'
-- ORDER BY ordinal_position;
