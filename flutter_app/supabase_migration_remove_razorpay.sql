-- ============================================================
-- Migration: Remove Razorpay, add manual payment tracking
-- Run this in Supabase SQL Editor (one time only)
-- ============================================================

-- Step 1: Drop Razorpay columns from payments table
ALTER TABLE payments
  DROP COLUMN IF EXISTS razorpay_order_id,
  DROP COLUMN IF EXISTS razorpay_payment_id,
  DROP COLUMN IF EXISTS receipt_url;

-- Step 2: Add manual payment tracking columns
ALTER TABLE payments
  ADD COLUMN IF NOT EXISTS note     TEXT   NOT NULL DEFAULT '',
  ADD COLUMN IF NOT EXISTS paid_at  BIGINT NOT NULL DEFAULT 0;

-- Step 3: Update the payment_status enum
-- PostgreSQL doesn't allow removing enum values directly,
-- so we replace the column type with a new clean enum.

-- 3a: Create the new enum
CREATE TYPE payment_status_new AS ENUM (
  'pending',
  'success',
  'overdue'
);

-- 3b: Convert the column to the new enum
--     (any old 'failed' or 'refunded' values become 'pending')
ALTER TABLE payments
  ALTER COLUMN status DROP DEFAULT;

ALTER TABLE payments
  ALTER COLUMN status TYPE payment_status_new
  USING (
    CASE status::TEXT
      WHEN 'success' THEN 'success'::payment_status_new
      WHEN 'overdue'  THEN 'overdue'::payment_status_new
      ELSE 'pending'::payment_status_new
    END
  );

ALTER TABLE payments
  ALTER COLUMN status SET DEFAULT 'pending'::payment_status_new;

-- 3c: Swap the enum names
DROP TYPE payment_status;
ALTER TYPE payment_status_new RENAME TO payment_status;

-- ============================================================
-- Done. Your payments table now has:
--   note    — optional note from resident when marking paid
--   paid_at — timestamp when resident tapped "Mark as Paid"
-- And payment_status only has: pending | success | overdue
-- ============================================================
