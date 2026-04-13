-- ============================================================
-- MGB Heights Society Management App — Supabase SQL Schema
-- ============================================================
-- Run this entire file in the Supabase SQL Editor to set up
-- the complete database from scratch.
-- ============================================================

-- Enable UUID generation
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- ============================================================
-- ENUMS
-- ============================================================

CREATE TYPE user_role AS ENUM (
  'admin',
  'resident',
  'tenant',
  'securityGuard',
  'worker',
  'maid'
);

CREATE TYPE approval_status AS ENUM (
  'pending',
  'approved',
  'rejected'
);

CREATE TYPE complaint_category AS ENUM (
  'plumbing',
  'electrical',
  'cleanliness',
  'security',
  'noise',
  'parking',
  'maintenance',
  'other'
);

CREATE TYPE complaint_status AS ENUM (
  'open',
  'inProgress',
  'resolved',
  'closed'
);

CREATE TYPE notice_category AS ENUM (
  'general',
  'maintenance',
  'event',
  'emergency',
  'payment',
  'security'
);

CREATE TYPE visitor_status AS ENUM (
  'pending',
  'approved',
  'rejected',
  'checkedIn',
  'checkedOut',
  'expired'
);

CREATE TYPE bill_status AS ENUM (
  'pending',
  'paid',
  'overdue',
  'cancelled'
);

CREATE TYPE payment_status AS ENUM (
  'pending',   -- bill created, not yet marked paid
  'success',   -- resident marked as paid in the app
  'overdue'    -- past due date and still unpaid
);

CREATE TYPE payment_type AS ENUM (
  'maintenance',
  'worker',
  'maid',
  'other'
);

CREATE TYPE worker_category AS ENUM (
  'plumber',
  'electrician',
  'carpenter',
  'cleaner',
  'gardener',
  'painter',
  'security',
  'other'
);

CREATE TYPE work_order_status AS ENUM (
  'pending',
  'accepted',
  'rejected',
  'inProgress',
  'completed',
  'cancelled'
);

-- ============================================================
-- TABLE: flats
-- Represents physical flat/apartment units in the society.
-- ============================================================

CREATE TABLE flats (
  id            UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
  flat_number   TEXT        NOT NULL,
  tower_block   TEXT        NOT NULL DEFAULT '',
  house_number  TEXT        NOT NULL DEFAULT '',
  floor         INTEGER     NOT NULL DEFAULT 0,
  is_occupied   BOOLEAN     NOT NULL DEFAULT FALSE,
  owner_id      UUID        REFERENCES auth.users(id) ON DELETE SET NULL,
  created_at    BIGINT      NOT NULL DEFAULT EXTRACT(EPOCH FROM NOW())::BIGINT * 1000,
  updated_at    BIGINT      NOT NULL DEFAULT EXTRACT(EPOCH FROM NOW())::BIGINT * 1000,

  UNIQUE (flat_number, tower_block)
);

-- ============================================================
-- TABLE: users
-- Mirrors Supabase auth.users with app-specific profile data.
-- id matches the auth.users UUID.
-- ============================================================

CREATE TABLE users (
  id                  UUID          PRIMARY KEY REFERENCES auth.users(id) ON DELETE CASCADE,
  email               TEXT          NOT NULL,
  name                TEXT          NOT NULL DEFAULT '',
  phone_number        TEXT          NOT NULL DEFAULT '',
  profile_photo_url   TEXT          NOT NULL DEFAULT '',
  id_proof_url        TEXT          NOT NULL DEFAULT '',
  role                user_role     NOT NULL DEFAULT 'resident',
  flat_number         TEXT          NOT NULL DEFAULT '',
  tower_block         TEXT          NOT NULL DEFAULT '',
  house_number        TEXT          NOT NULL DEFAULT '',
  approval_status     approval_status NOT NULL DEFAULT 'pending',
  is_blocked          BOOLEAN       NOT NULL DEFAULT FALSE,
  is_profile_complete BOOLEAN       NOT NULL DEFAULT FALSE,
  is_onboarded        BOOLEAN       NOT NULL DEFAULT FALSE,
  tenant_of           UUID          REFERENCES users(id) ON DELETE SET NULL,  -- set if this user is a tenant of a resident
  created_at          BIGINT        NOT NULL DEFAULT EXTRACT(EPOCH FROM NOW())::BIGINT * 1000,
  updated_at          BIGINT        NOT NULL DEFAULT EXTRACT(EPOCH FROM NOW())::BIGINT * 1000
);

-- ============================================================
-- TABLE: notices
-- Society-wide announcements posted by admin.
-- ============================================================

CREATE TABLE notices (
  id            UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
  title         TEXT            NOT NULL DEFAULT '',
  content       TEXT            NOT NULL DEFAULT '',   -- mapped to 'body' in Flutter model
  image_url     TEXT            NOT NULL DEFAULT '',
  author_id     UUID            REFERENCES users(id) ON DELETE SET NULL,   -- mapped to 'createdBy'
  author_name   TEXT            NOT NULL DEFAULT '',                        -- mapped to 'createdByName'
  category      notice_category NOT NULL DEFAULT 'general',
  target_roles  TEXT[]          NOT NULL DEFAULT '{}',  -- e.g. ["resident","tenant"]
  is_pinned     BOOLEAN         NOT NULL DEFAULT FALSE,
  created_at    BIGINT          NOT NULL DEFAULT EXTRACT(EPOCH FROM NOW())::BIGINT * 1000,
  updated_at    BIGINT          NOT NULL DEFAULT EXTRACT(EPOCH FROM NOW())::BIGINT * 1000
);

-- ============================================================
-- TABLE: complaints
-- Complaints raised by residents/tenants.
-- ============================================================

CREATE TABLE complaints (
  id              UUID                PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id         UUID                REFERENCES users(id) ON DELETE SET NULL,   -- 'residentId'
  user_name       TEXT                NOT NULL DEFAULT '',                        -- 'residentName'
  flat_number     TEXT                NOT NULL DEFAULT '',
  tower_block     TEXT                NOT NULL DEFAULT '',
  title           TEXT                NOT NULL DEFAULT '',
  description     TEXT                NOT NULL DEFAULT '',
  category        complaint_category  NOT NULL DEFAULT 'other',
  status          complaint_status    NOT NULL DEFAULT 'open',
  image_url       TEXT                NOT NULL DEFAULT '',   -- single image (see note)
  admin_response  TEXT                NOT NULL DEFAULT '',   -- mapped to 'resolution' in Flutter
  created_at      BIGINT              NOT NULL DEFAULT EXTRACT(EPOCH FROM NOW())::BIGINT * 1000,
  updated_at      BIGINT              NOT NULL DEFAULT EXTRACT(EPOCH FROM NOW())::BIGINT * 1000
);

-- Note: The Flutter model stores imageUrls as List<String>, but the Supabase column
-- is a single TEXT 'image_url'. If you want multi-image support, change to TEXT[].

-- ============================================================
-- TABLE: visitors
-- Visitor log managed by security guards and residents.
-- ============================================================

CREATE TABLE visitors (
  id              UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
  resident_id     UUID            REFERENCES users(id) ON DELETE SET NULL,
  resident_name   TEXT            NOT NULL DEFAULT '',
  flat_number     TEXT            NOT NULL DEFAULT '',
  tower_block     TEXT            NOT NULL DEFAULT '',
  visitor_name    TEXT            NOT NULL DEFAULT '',    -- mapped to 'name' in Flutter model
  visitor_phone   TEXT            NOT NULL DEFAULT '',    -- mapped to 'phoneNumber'
  purpose         TEXT            NOT NULL DEFAULT '',
  vehicle_number  TEXT            NOT NULL DEFAULT '',
  photo_url       TEXT            NOT NULL DEFAULT '',
  id_proof_url    TEXT            NOT NULL DEFAULT '',
  status          visitor_status  NOT NULL DEFAULT 'pending',
  entry_time      BIGINT          NOT NULL DEFAULT 0,
  exit_time       BIGINT          NOT NULL DEFAULT 0,
  created_at      BIGINT          NOT NULL DEFAULT EXTRACT(EPOCH FROM NOW())::BIGINT * 1000,
  updated_at      BIGINT          NOT NULL DEFAULT EXTRACT(EPOCH FROM NOW())::BIGINT * 1000
);

-- ============================================================
-- TABLE: maintenance_bills
-- Monthly maintenance bills issued to residents/tenants.
-- ============================================================

CREATE TABLE maintenance_bills (
  id              UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
  flat_id         UUID        REFERENCES flats(id) ON DELETE SET NULL,
  flat_number     TEXT        NOT NULL DEFAULT '',
  tower_block     TEXT        NOT NULL DEFAULT '',
  resident_id     UUID        REFERENCES users(id) ON DELETE SET NULL,
  resident_name   TEXT        NOT NULL DEFAULT '',
  title           TEXT        NOT NULL DEFAULT '',
  amount          NUMERIC(10,2) NOT NULL DEFAULT 0.00,
  late_fee        NUMERIC(10,2) NOT NULL DEFAULT 0.00,
  total_amount    NUMERIC(10,2) NOT NULL DEFAULT 0.00,
  month           TEXT        NOT NULL DEFAULT '',   -- e.g. "2025-01"
  year            INTEGER     NOT NULL DEFAULT 0,
  due_date        BIGINT      NOT NULL DEFAULT 0,
  status          bill_status NOT NULL DEFAULT 'pending',
  description     TEXT        NOT NULL DEFAULT '',
  paid_at         BIGINT      NOT NULL DEFAULT 0,
  created_at      BIGINT      NOT NULL DEFAULT EXTRACT(EPOCH FROM NOW())::BIGINT * 1000,
  updated_at      BIGINT      NOT NULL DEFAULT EXTRACT(EPOCH FROM NOW())::BIGINT * 1000
);

-- ============================================================
-- TABLE: payments
-- Payment transactions linked to maintenance bills or workers.
-- ============================================================

CREATE TABLE payments (
  id                    UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
  bill_id               UUID            REFERENCES maintenance_bills(id) ON DELETE SET NULL,
  user_id               UUID            REFERENCES users(id) ON DELETE SET NULL,
  user_name             TEXT            NOT NULL DEFAULT '',
  flat_number           TEXT            NOT NULL DEFAULT '',
  tower_block           TEXT            NOT NULL DEFAULT '',
  amount                NUMERIC(10,2)   NOT NULL DEFAULT 0.00,
  status                payment_status  NOT NULL DEFAULT 'pending',
  type                  payment_type    NOT NULL DEFAULT 'maintenance',
  note                  TEXT            NOT NULL DEFAULT '',   -- optional note from resident when marking paid
  paid_at               BIGINT          NOT NULL DEFAULT 0,    -- timestamp when resident tapped "Mark as Paid"
  created_at            BIGINT          NOT NULL DEFAULT EXTRACT(EPOCH FROM NOW())::BIGINT * 1000,
  updated_at            BIGINT          NOT NULL DEFAULT EXTRACT(EPOCH FROM NOW())::BIGINT * 1000
);

-- ============================================================
-- TABLE: workers
-- Worker profile data (extra profile on top of users table).
-- ============================================================

CREATE TABLE workers (
  id                  UUID              PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id             UUID              NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  name                TEXT              NOT NULL DEFAULT '',
  phone_number        TEXT              NOT NULL DEFAULT '',
  profile_photo_url   TEXT              NOT NULL DEFAULT '',
  id_proof_url        TEXT              NOT NULL DEFAULT '',
  skills              TEXT[]            NOT NULL DEFAULT '{}',
  category            worker_category   NOT NULL DEFAULT 'other',
  is_available        BOOLEAN           NOT NULL DEFAULT TRUE,
  is_duty_on          BOOLEAN           NOT NULL DEFAULT FALSE,
  rating              NUMERIC(3,2)      NOT NULL DEFAULT 0.00,
  total_ratings       INTEGER           NOT NULL DEFAULT 0,
  total_jobs          INTEGER           NOT NULL DEFAULT 0,
  total_earnings      NUMERIC(12,2)     NOT NULL DEFAULT 0.00,
  assigned_flats      TEXT[]            NOT NULL DEFAULT '{}',
  created_at          BIGINT            NOT NULL DEFAULT EXTRACT(EPOCH FROM NOW())::BIGINT * 1000,
  updated_at          BIGINT            NOT NULL DEFAULT EXTRACT(EPOCH FROM NOW())::BIGINT * 1000,

  UNIQUE (user_id)
);

-- ============================================================
-- TABLE: work_orders
-- Service requests raised by residents for workers.
-- ============================================================

CREATE TABLE work_orders (
  id              UUID                PRIMARY KEY DEFAULT gen_random_uuid(),
  worker_id       UUID                REFERENCES workers(id) ON DELETE SET NULL,
  worker_name     TEXT                NOT NULL DEFAULT '',
  flat_id         UUID                REFERENCES flats(id) ON DELETE SET NULL,
  flat_number     TEXT                NOT NULL DEFAULT '',
  tower_block     TEXT                NOT NULL DEFAULT '',
  resident_id     UUID                REFERENCES users(id) ON DELETE SET NULL,
  resident_name   TEXT                NOT NULL DEFAULT '',
  title           TEXT                NOT NULL DEFAULT '',
  description     TEXT                NOT NULL DEFAULT '',
  category        worker_category     NOT NULL DEFAULT 'other',
  status          work_order_status   NOT NULL DEFAULT 'pending',
  amount          NUMERIC(10,2)       NOT NULL DEFAULT 0.00,
  is_paid         BOOLEAN             NOT NULL DEFAULT FALSE,
  payment_id      UUID                REFERENCES payments(id) ON DELETE SET NULL,
  rating          NUMERIC(3,2)        NOT NULL DEFAULT 0.00,
  feedback        TEXT                NOT NULL DEFAULT '',
  scheduled_at    BIGINT              NOT NULL DEFAULT 0,
  started_at      BIGINT              NOT NULL DEFAULT 0,
  completed_at    BIGINT              NOT NULL DEFAULT 0,
  created_at      BIGINT              NOT NULL DEFAULT EXTRACT(EPOCH FROM NOW())::BIGINT * 1000,
  updated_at      BIGINT              NOT NULL DEFAULT EXTRACT(EPOCH FROM NOW())::BIGINT * 1000
);

-- ============================================================
-- TABLE: maid_slots
-- Recurring maid schedule slots assigned to flats.
-- ============================================================

CREATE TABLE maid_slots (
  id            UUID      PRIMARY KEY DEFAULT gen_random_uuid(),
  maid_id       UUID      REFERENCES users(id) ON DELETE CASCADE,
  maid_name     TEXT      NOT NULL DEFAULT '',
  flat_id       UUID      REFERENCES flats(id) ON DELETE SET NULL,
  flat_number   TEXT      NOT NULL DEFAULT '',
  tower_block   TEXT      NOT NULL DEFAULT '',
  resident_id   UUID      REFERENCES users(id) ON DELETE SET NULL,
  time_slot     TEXT      NOT NULL DEFAULT '',    -- e.g. "08:00 AM - 09:00 AM"
  days          TEXT[]    NOT NULL DEFAULT '{}',  -- e.g. ["MON","TUE","WED"]
  is_active     BOOLEAN   NOT NULL DEFAULT TRUE,
  created_at    BIGINT    NOT NULL DEFAULT EXTRACT(EPOCH FROM NOW())::BIGINT * 1000,
  updated_at    BIGINT    NOT NULL DEFAULT EXTRACT(EPOCH FROM NOW())::BIGINT * 1000
);

-- ============================================================
-- TABLE: maid_attendance
-- Daily duty check-in/check-out records for maids.
-- ============================================================

CREATE TABLE maid_attendance (
  id              UUID    PRIMARY KEY DEFAULT gen_random_uuid(),
  maid_id         UUID    REFERENCES users(id) ON DELETE CASCADE,
  maid_name       TEXT    NOT NULL DEFAULT '',
  flat_number     TEXT    NOT NULL DEFAULT '',
  tower_block     TEXT    NOT NULL DEFAULT '',
  is_duty_on      BOOLEAN NOT NULL DEFAULT FALSE,
  duty_on_time    BIGINT  NOT NULL DEFAULT 0,
  duty_off_time   BIGINT  NOT NULL DEFAULT 0,
  date            BIGINT  NOT NULL DEFAULT 0,
  created_at      BIGINT  NOT NULL DEFAULT EXTRACT(EPOCH FROM NOW())::BIGINT * 1000
);

-- ============================================================
-- TABLE: shop_listings  (referenced in AppConstants)
-- Community marketplace / shop listings.
-- ============================================================

CREATE TABLE shop_listings (
  id              UUID    PRIMARY KEY DEFAULT gen_random_uuid(),
  seller_id       UUID    REFERENCES users(id) ON DELETE CASCADE,
  seller_name     TEXT    NOT NULL DEFAULT '',
  title           TEXT    NOT NULL DEFAULT '',
  description     TEXT    NOT NULL DEFAULT '',
  price           NUMERIC(10,2) NOT NULL DEFAULT 0.00,
  image_urls      TEXT[]  NOT NULL DEFAULT '{}',
  category        TEXT    NOT NULL DEFAULT 'other',
  is_available    BOOLEAN NOT NULL DEFAULT TRUE,
  is_free         BOOLEAN NOT NULL DEFAULT FALSE,
  is_sold         BOOLEAN NOT NULL DEFAULT FALSE,
  flat_number     TEXT    NOT NULL DEFAULT '',
  tower_block     TEXT    NOT NULL DEFAULT '',
  expires_at      BIGINT  NOT NULL DEFAULT 0,
  created_at      BIGINT  NOT NULL DEFAULT EXTRACT(EPOCH FROM NOW())::BIGINT * 1000,
  updated_at      BIGINT  NOT NULL DEFAULT EXTRACT(EPOCH FROM NOW())::BIGINT * 1000
);

-- ============================================================
-- TABLE: edit_requests  (referenced in AppConstants)
-- Requests from users to edit profile or flat data.
-- ============================================================

CREATE TABLE edit_requests (
  id              UUID    PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id         UUID    REFERENCES users(id) ON DELETE CASCADE,
  user_name       TEXT    NOT NULL DEFAULT '',
  field_name      TEXT    NOT NULL DEFAULT '',   -- which field to change
  old_value       TEXT    NOT NULL DEFAULT '',
  new_value       TEXT    NOT NULL DEFAULT '',
  status          approval_status NOT NULL DEFAULT 'pending',
  admin_note      TEXT    NOT NULL DEFAULT '',
  created_at      BIGINT  NOT NULL DEFAULT EXTRACT(EPOCH FROM NOW())::BIGINT * 1000,
  updated_at      BIGINT  NOT NULL DEFAULT EXTRACT(EPOCH FROM NOW())::BIGINT * 1000
);

-- ============================================================
-- INDEXES (for common query patterns)
-- ============================================================

-- users
CREATE INDEX idx_users_role            ON users(role);
CREATE INDEX idx_users_flat_number     ON users(flat_number);
CREATE INDEX idx_users_approval_status ON users(approval_status);
CREATE INDEX idx_users_tenant_of       ON users(tenant_of);

-- complaints
CREATE INDEX idx_complaints_user_id    ON complaints(user_id);
CREATE INDEX idx_complaints_status     ON complaints(status);
CREATE INDEX idx_complaints_created_at ON complaints(created_at DESC);

-- notices
CREATE INDEX idx_notices_is_pinned     ON notices(is_pinned);
CREATE INDEX idx_notices_created_at    ON notices(created_at DESC);

-- visitors
CREATE INDEX idx_visitors_resident_id  ON visitors(resident_id);
CREATE INDEX idx_visitors_status       ON visitors(status);
CREATE INDEX idx_visitors_created_at   ON visitors(created_at DESC);

-- maintenance_bills
CREATE INDEX idx_bills_resident_id     ON maintenance_bills(resident_id);
CREATE INDEX idx_bills_flat_number     ON maintenance_bills(flat_number);
CREATE INDEX idx_bills_status          ON maintenance_bills(status);
CREATE INDEX idx_bills_created_at      ON maintenance_bills(created_at DESC);

-- payments
CREATE INDEX idx_payments_user_id      ON payments(user_id);
CREATE INDEX idx_payments_bill_id      ON payments(bill_id);
CREATE INDEX idx_payments_created_at   ON payments(created_at DESC);

-- work_orders
CREATE INDEX idx_work_orders_worker_id   ON work_orders(worker_id);
CREATE INDEX idx_work_orders_resident_id ON work_orders(resident_id);
CREATE INDEX idx_work_orders_status      ON work_orders(status);
CREATE INDEX idx_work_orders_created_at  ON work_orders(created_at DESC);

-- workers
CREATE INDEX idx_workers_user_id       ON workers(user_id);
CREATE INDEX idx_workers_category      ON workers(category);
CREATE INDEX idx_workers_is_available  ON workers(is_available);

-- maid_slots
CREATE INDEX idx_maid_slots_maid_id    ON maid_slots(maid_id);
CREATE INDEX idx_maid_slots_resident_id ON maid_slots(resident_id);

-- maid_attendance
CREATE INDEX idx_maid_attendance_maid_id ON maid_attendance(maid_id);
CREATE INDEX idx_maid_attendance_date    ON maid_attendance(date DESC);

-- ============================================================
-- ROW LEVEL SECURITY (RLS)
-- Enable for all tables — policies enforce per-role access.
-- ============================================================

ALTER TABLE users              ENABLE ROW LEVEL SECURITY;
ALTER TABLE flats               ENABLE ROW LEVEL SECURITY;
ALTER TABLE notices             ENABLE ROW LEVEL SECURITY;
ALTER TABLE complaints          ENABLE ROW LEVEL SECURITY;
ALTER TABLE visitors            ENABLE ROW LEVEL SECURITY;
ALTER TABLE maintenance_bills   ENABLE ROW LEVEL SECURITY;
ALTER TABLE payments            ENABLE ROW LEVEL SECURITY;
ALTER TABLE workers             ENABLE ROW LEVEL SECURITY;
ALTER TABLE work_orders         ENABLE ROW LEVEL SECURITY;
ALTER TABLE maid_slots          ENABLE ROW LEVEL SECURITY;
ALTER TABLE maid_attendance     ENABLE ROW LEVEL SECURITY;
ALTER TABLE shop_listings       ENABLE ROW LEVEL SECURITY;
ALTER TABLE edit_requests       ENABLE ROW LEVEL SECURITY;

-- ============================================================
-- HELPER FUNCTION: get the role of the calling user
-- ============================================================

CREATE OR REPLACE FUNCTION get_my_role()
RETURNS TEXT AS $$
  SELECT role::TEXT FROM users WHERE id = auth.uid();
$$ LANGUAGE SQL STABLE SECURITY DEFINER;

-- ============================================================
-- RLS POLICIES
-- ============================================================

-- ── users ────────────────────────────────────────────────────

-- Anyone authenticated can read user records (needed for name lookups)
CREATE POLICY "users: read all authenticated"
  ON users FOR SELECT
  USING (auth.uid() IS NOT NULL);

-- Admins can update any user; users can only update their own profile
CREATE POLICY "users: update own or admin"
  ON users FOR UPDATE
  USING (
    id = auth.uid()
    OR get_my_role() = 'admin'
  );

-- Insert handled by auth trigger (see below) — block direct inserts by non-admins
CREATE POLICY "users: insert self or admin"
  ON users FOR INSERT
  WITH CHECK (
    id = auth.uid()
    OR get_my_role() = 'admin'
  );

-- Only admins can delete users
CREATE POLICY "users: delete by admin only"
  ON users FOR DELETE
  USING (get_my_role() = 'admin');

-- ── flats ────────────────────────────────────────────────────

CREATE POLICY "flats: read all authenticated"
  ON flats FOR SELECT
  USING (auth.uid() IS NOT NULL);

CREATE POLICY "flats: write by admin only"
  ON flats FOR ALL
  USING (get_my_role() = 'admin')
  WITH CHECK (get_my_role() = 'admin');

-- ── notices ──────────────────────────────────────────────────

CREATE POLICY "notices: read all authenticated"
  ON notices FOR SELECT
  USING (auth.uid() IS NOT NULL);

CREATE POLICY "notices: write by admin only"
  ON notices FOR ALL
  USING (get_my_role() = 'admin')
  WITH CHECK (get_my_role() = 'admin');

-- ── complaints ───────────────────────────────────────────────

-- Residents/tenants see only their own; admins see all
CREATE POLICY "complaints: read own or admin"
  ON complaints FOR SELECT
  USING (
    user_id = auth.uid()
    OR get_my_role() = 'admin'
  );

-- Residents/tenants can create complaints
CREATE POLICY "complaints: insert by resident or tenant"
  ON complaints FOR INSERT
  WITH CHECK (
    user_id = auth.uid()
    AND get_my_role() IN ('resident', 'tenant')
  );

-- Admin can update (change status/resolution); owner can update own
CREATE POLICY "complaints: update own or admin"
  ON complaints FOR UPDATE
  USING (
    user_id = auth.uid()
    OR get_my_role() = 'admin'
  );

-- ── visitors ─────────────────────────────────────────────────

-- Guards and admins see all; residents see their own
CREATE POLICY "visitors: read"
  ON visitors FOR SELECT
  USING (
    resident_id = auth.uid()
    OR get_my_role() IN ('admin', 'securityGuard')
  );

CREATE POLICY "visitors: insert by resident or guard"
  ON visitors FOR INSERT
  WITH CHECK (
    get_my_role() IN ('resident', 'tenant', 'securityGuard', 'admin')
  );

CREATE POLICY "visitors: update by guard or admin"
  ON visitors FOR UPDATE
  USING (
    get_my_role() IN ('securityGuard', 'admin')
    OR resident_id = auth.uid()
  );

-- ── maintenance_bills ────────────────────────────────────────

CREATE POLICY "bills: read own or admin"
  ON maintenance_bills FOR SELECT
  USING (
    resident_id = auth.uid()
    OR get_my_role() = 'admin'
  );

CREATE POLICY "bills: write by admin only"
  ON maintenance_bills FOR INSERT
  WITH CHECK (get_my_role() = 'admin');

CREATE POLICY "bills: update by admin"
  ON maintenance_bills FOR UPDATE
  USING (get_my_role() = 'admin');

-- ── payments ─────────────────────────────────────────────────

CREATE POLICY "payments: read own or admin"
  ON payments FOR SELECT
  USING (
    user_id = auth.uid()
    OR get_my_role() = 'admin'
  );

CREATE POLICY "payments: insert by self"
  ON payments FOR INSERT
  WITH CHECK (user_id = auth.uid());

CREATE POLICY "payments: update by admin"
  ON payments FOR UPDATE
  USING (get_my_role() = 'admin');

-- ── workers ──────────────────────────────────────────────────

CREATE POLICY "workers: read all authenticated"
  ON workers FOR SELECT
  USING (auth.uid() IS NOT NULL);

CREATE POLICY "workers: write own or admin"
  ON workers FOR ALL
  USING (
    user_id = auth.uid()
    OR get_my_role() = 'admin'
  )
  WITH CHECK (
    user_id = auth.uid()
    OR get_my_role() = 'admin'
  );

-- ── work_orders ──────────────────────────────────────────────

CREATE POLICY "work_orders: read relevant parties"
  ON work_orders FOR SELECT
  USING (
    resident_id = auth.uid()
    OR worker_id IN (SELECT id FROM workers WHERE user_id = auth.uid())
    OR get_my_role() = 'admin'
  );

CREATE POLICY "work_orders: insert by resident or admin"
  ON work_orders FOR INSERT
  WITH CHECK (
    resident_id = auth.uid()
    OR get_my_role() = 'admin'
  );

CREATE POLICY "work_orders: update by worker, resident, or admin"
  ON work_orders FOR UPDATE
  USING (
    resident_id = auth.uid()
    OR worker_id IN (SELECT id FROM workers WHERE user_id = auth.uid())
    OR get_my_role() = 'admin'
  );

-- ── maid_slots ───────────────────────────────────────────────

CREATE POLICY "maid_slots: read own resident or maid or admin"
  ON maid_slots FOR SELECT
  USING (
    resident_id = auth.uid()
    OR maid_id = auth.uid()
    OR get_my_role() = 'admin'
  );

CREATE POLICY "maid_slots: write by resident or admin"
  ON maid_slots FOR ALL
  USING (
    resident_id = auth.uid()
    OR get_my_role() = 'admin'
  )
  WITH CHECK (
    resident_id = auth.uid()
    OR get_my_role() = 'admin'
  );

-- ── maid_attendance ──────────────────────────────────────────

CREATE POLICY "maid_attendance: read own maid and admin"
  ON maid_attendance FOR SELECT
  USING (
    maid_id = auth.uid()
    OR get_my_role() = 'admin'
  );

CREATE POLICY "maid_attendance: insert by maid"
  ON maid_attendance FOR INSERT
  WITH CHECK (maid_id = auth.uid());

CREATE POLICY "maid_attendance: update by maid"
  ON maid_attendance FOR UPDATE
  USING (maid_id = auth.uid());

-- ── shop_listings ────────────────────────────────────────────

CREATE POLICY "shop_listings: read all authenticated"
  ON shop_listings FOR SELECT
  USING (auth.uid() IS NOT NULL);

CREATE POLICY "shop_listings: write own or admin"
  ON shop_listings FOR ALL
  USING (
    seller_id = auth.uid()
    OR get_my_role() = 'admin'
  )
  WITH CHECK (
    seller_id = auth.uid()
    OR get_my_role() = 'admin'
  );

-- ── edit_requests ────────────────────────────────────────────

CREATE POLICY "edit_requests: read own or admin"
  ON edit_requests FOR SELECT
  USING (
    user_id = auth.uid()
    OR get_my_role() = 'admin'
  );

CREATE POLICY "edit_requests: insert by self"
  ON edit_requests FOR INSERT
  WITH CHECK (user_id = auth.uid());

CREATE POLICY "edit_requests: update by admin"
  ON edit_requests FOR UPDATE
  USING (get_my_role() = 'admin');

-- ============================================================
-- TRIGGER: auto-create user profile row on sign-up
-- Fires after a new row is inserted into auth.users by Supabase Auth.
-- ============================================================

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

CREATE OR REPLACE TRIGGER on_auth_user_created
  AFTER INSERT ON auth.users
  FOR EACH ROW
  EXECUTE FUNCTION handle_new_auth_user();

-- ============================================================
-- STORAGE BUCKETS (run separately in Supabase Dashboard
-- or via the Storage API — SQL below is for documentation)
-- ============================================================
-- Bucket: profiles       (public: true)  — profile photos
-- Bucket: id-proofs      (public: false) — ID proof documents
-- Bucket: visitors       (public: false) — visitor photos/IDs
-- Bucket: complaints     (public: false) — complaint images
-- Bucket: notices        (public: true)  — notice images
-- Bucket: shop           (public: true)  — shop listing images
-- ============================================================

-- ============================================================
-- END OF SCHEMA
-- ============================================================
