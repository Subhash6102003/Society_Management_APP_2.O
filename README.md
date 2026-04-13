# 🏢 MGB Heights — Society Management App

> Full-featured Android society management app. Backend: **Supabase**. 6 user roles, each with a dedicated panel.

---

## 👥 User Roles Overview

| Role | Panel | Description |
|------|-------|-------------|
| `ADMIN` | Admin Panel | Full control over society, users, bills, workers |
| `RESIDENT` | Resident Panel | Flat owner — manages flat, tenants, bookings, payments |
| `TENANT` | Tenant Panel | Rented flat — daily features like resident |
| `SECURITY_GUARD` | Guard Panel | Gate entry, visitor check-in/out |
| `WORKER` | Worker Panel | Service worker — plumber, electrician, carpenter, etc. |
| `MAID` | Maid Panel | Cleaning/service slots with attendance tracking |

---

## 🔴 ADMIN

### Dashboard
- Total residents, tenants, workers, guards count
- Pending approvals, unpaid bills, open complaints, today's visitor entries

### User Management
- Approve / Reject new registrations
- Block / Unblock any user
- Handle **Edit Requests** — user requests profile change, admin approves/rejects
- View full profile: photo, ID proof, flat, role

### Resident & Tenant Management
- View all residents/tenants with flat details
- See payment history, complaints, maid & worker bookings per resident/tenant

### Worker Monitoring (Admin Sees Everything)
| Data | Visible |
|------|---------|
| Worker name & category | ✅ |
| Who booked (Resident/Tenant name) | ✅ |
| Flat number | ✅ |
| Job title & description | ✅ |
| Amount charged & paid | ✅ |
| Payment date/time | ✅ |
| Job status | ✅ |
| Rating given after job | ✅ |
| Worker lifetime earnings | ✅ |

### Maid Activity Monitoring (Admin Sees Everything)
| Data | Visible |
|------|---------|
| Maid name & assigned flats | ✅ |
| Daily duty ON/OFF timestamps | ✅ |
| Attendance history (present/absent/late) | ✅ |
| Which resident/tenant hired them | ✅ |
| Payment records per flat per month | ✅ |

### Maintenance Bills Management
- Create monthly bills per flat (default ₹5,000/month)
- Add line items: water, lift, parking, etc.
- Auto late fee: **2% after 15-day grace period**
- Bill statuses: Pending / Paid / Overdue / Partially Paid / Waived
- Manual cash/cheque entry option
- View all Razorpay transaction IDs and receipts

### Payments Monitoring
- All payment types: Maintenance / Worker Payment / Service Charge / Late Fee
- Filter by type, flat, date, status
- Download receipts

### Notices Management
- Create notices targeted to specific roles or all users
- Categories: General / Maintenance / Security / Event / Emergency / Rule
- Priority: Low / Normal / High / Urgent
- Mark as **Emergency** (highlighted for all)
- Set expiry date, attach image
- Track read receipts per user

### Complaints Management
- View all complaints from all flats
- Assign complaint to a specific worker
- Status flow: `OPEN → IN_PROGRESS → RESOLVED → CLOSED / REJECTED`
- View attached photos, set resolution note

### Visitor Log (Full Society)
- All visitor entries across all gates
- Filter by date, flat, guard, status
- View visitor photos, ID proofs, vehicle details
- Flag / Blacklist a visitor

### Guards Management
- View all guards, duty status
- Monitor visitor entries handled per guard
- Daily check-in/check-out log

### Admin Shop & Media
- Moderate community shop listings
- View all uploaded media (photos, ID proofs, receipts)

---

## 🟢 RESIDENT

### Home Dashboard
- Pending bill, active complaints, unread notices, maid attendance today
- Quick actions: Book Worker, Add Visitor, Raise Complaint, Pay Bill

### 💳 My Bills
- View monthly maintenance bills with full breakdown
- Pay online via **Razorpay** (UPI, card, net banking)
- Download receipt, view payment history

### 🔧 Book a Worker
- Browse workers by category: Plumber / Electrician / Carpenter / Cleaner / Gardener / Painter / Security
- View worker profile: photo, rating, total jobs, skills, availability
- Book with date/time + job description
- Track booking: `Pending → Accepted → In Progress → Completed`
- **Pay worker** through app after job completion
- Rate & review worker after completion
- View all past bookings

### 🧹 My Maid
- View maid(s) assigned to their flat
- Monitor **maid attendance** — duty ON/OFF log with timestamps
- See if maid arrived on time, was late, or absent
- Track monthly maid payment records

### 🏠 My Tenants
- View tenants living in their flat
- See tenant profile, complaints, bookings, payment activity
- Remove/update tenant association

### 📋 Complaints
- Raise complaint: title, description, category, priority, photo evidence
- Track status and resolution note
- See which worker was assigned

### 📢 Notices
- View all society notices, filter by category
- Emergency notices shown prominently
- Mark as read

### 🚶 Add Visitor
- Pre-register visitor (name, phone, purpose)
- Guard gets notification to allow/deny entry
- View visitor history for their flat
- Mark frequent visitors

### 🛒 Community Shop
- Browse, post, manage buy/sell listings with photos

---

## 🔵 TENANT

> Same daily features as Resident — except cannot manage other tenants.

- ✅ My Bills + Razorpay payment
- ✅ Book a Worker (full booking + payment + rating flow)
- ✅ My Maid (attendance monitoring)
- ✅ Raise & track Complaints
- ✅ View Notices
- ✅ Community Shop
- ✅ Profile (edit request goes to admin)

---

## 🟡 SECURITY GUARD

### Home / Dashboard
- Today's visitor count: checked-in / checked-out / pending

### Add Visitor
- Register visitor: name, phone, purpose, flat to visit
- Capture visitor **photo** + **ID proof**
- Record **vehicle number & type** (Two-wheeler / Four-wheeler / Auto / Commercial)
- Send approval notification to resident/tenant
- Mark as frequent visitor

### Visitor Check-In / Check-Out
- View pending approvals
- **Approve** → allow entry, record entry timestamp
- **Deny** → reject with reason
- **Check-Out** → record exit timestamp
- Auto-expire if no response in 30 minutes

### Visitor Log
- Full daily log filtered by flat, status, name, vehicle
- Flag suspicious visitors
- Blacklist a visitor

---

## 🟠 WORKER

### Dashboard
- Today's bookings summary
- Total jobs completed (lifetime), total earnings, star rating
- **Duty toggle**: Mark ON/OFF (shows availability to residents)

### My Bookings
- View all incoming job requests with status filter
- Each booking: resident name, flat, job title, description, scheduled time, amount

### Booking Detail
- **Accept** or **Reject** a booking request
- Mark **In Progress** when work starts
- Mark **Completed** when done
- View payment status and resident's rating/feedback

### Earnings
- All payments tracked per job → visible in Admin panel:
  - Who paid, how much, for what job, which flat, date/time

### Community Shop + Profile
- Browse/post listings
- Update skills, category, view rating breakdown

---

## 🩷 MAID

### Dashboard
- Today's assigned flats and time slots
- **Duty toggle**: Mark ON/OFF (timestamp recorded)

### My Flat Slots
- View all assigned flats: flat number, tower, resident name, time slot, payment status

### Add Slot
- Register new flat assignment with time (morning/afternoon/evening)

### Attendance (Visible to Resident, Tenant & Admin)
- Duty ON → check-in timestamp recorded
- Duty OFF → check-out timestamp recorded
- Date-wise attendance history available to resident/tenant/admin

---

## ⚙️ Worker & Maid Booking Flow

```
Resident/Tenant → Browse Workers → Select → Create Booking
      ↓
Worker: Accept / Reject
      ↓
Worker marks IN PROGRESS → COMPLETED
      ↓
Resident/Tenant PAYS via app
      ↓
Resident/Tenant RATES worker
      ↓
ADMIN sees: payment record, worker earnings, job history
```

```
Maid → Opens App → Marks DUTY ON (timestamp saved)
     → Serves assigned flats
     → Marks DUTY OFF (timestamp saved)

Resident/Tenant sees: daily duty log, on-time/late/absent
Admin sees: all maids, all flats, full attendance + payment history
```

---

## 🚪 Visitor Flow

```
Guard registers visitor → Notifies Resident/Tenant
      ↓
Resident APPROVES → Entry allowed → Check-In logged
Resident DENIES  → Entry denied with reason
No response (30 min) → Auto-expired
      ↓
On exit: Guard marks Check-Out → Exit timestamp logged
      ↓
Admin sees: full log, photos, guard name, timestamps, vehicle info
```

---

## 🔐 Auth & Approval Flow

```
User Signs Up (Email + Password)
      ↓
Selects Role → Creates Profile (name, phone, photo, ID proof, flat no.)
      ↓
Status: PENDING
      ↓
Admin: APPROVE → Full panel access
Admin: REJECT  → "Access denied. Contact admin."
      ↓
PENDING → "Waiting for admin approval"
BLOCKED → Locked out immediately
```

### User UID Format
- Pattern: `MGBxxx` (e.g., `MGB001`, `MGB042`)
- Links all data: bills, complaints, bookings, payments to the user

---

## 🗄 Key Database Tables (Supabase)

| Table | Key Fields |
|-------|-----------|
| `users` | id (UID), email, password_hash, role, flat_number, is_approved, is_blocked |
| `flats` | flat_number, tower_block, owner_id, tenant_id, assigned_workers[] |
| `workers` | user_id, category, skills, is_available, is_duty_on, rating, total_earnings, assigned_flats[] |
| `work_orders` | worker_id, resident_id, flat_id, title, status, amount, is_paid, scheduled_at, completed_at |
| `maintenance_bills` | flat_id, resident_id, amount, late_fee, total_amount, month, due_date, status |
| `payments` | user_id, bill_id, amount, type, status, razorpay_id, receipt_url, paid_at |
| `visitors` | name, phone, flat_id, guard_id, photo_url, vehicle_number, status, entry_time, exit_time, is_blacklisted |
| `complaints` | flat_id, user_id, title, category, priority, status, assigned_worker_id, image_urls, resolution |
| `notices` | title, body, category, priority, target_roles[], is_emergency, expires_at, read_by[] |
| `edit_requests` | user_id, requested_changes, current_values, status, admin_note |

---

## 🛠 Tech Stack

| Layer | Technology |
|-------|-----------|
| Android | Kotlin, MVVM, Hilt DI, Room DB, Navigation Component |
| iOS | SwiftUI + KMP Shared Logic |
| Shared Logic | Kotlin Multiplatform (KMP) |
| Backend | Supabase (PostgreSQL) |
| File Storage | Supabase Storage Buckets |
| Payments | Razorpay |
| Local Cache | Room Database |
| Background Sync | WorkManager |
| Push Notifications | Firebase Cloud Messaging (FCM) |

---

## 📁 Project Structure

```
MGB Heights/
├── androidApp/src/main/java/com/mgbheights/android/
│   ├── ui/
│   │   ├── admin/       → All admin screens (dashboard, users, bills, workers, maids, guards, complaints, notices, visitors)
│   │   ├── resident/    → Resident panel (home, bills, bookings, maid, tenants, complaints, notices, shop, visitor)
│   │   ├── tenant/      → Tenant panel (home, bills, bookings, maid, complaints, notices, shop)
│   │   ├── guard/       → Guard panel (home, add visitor, check-in/out, visitor log)
│   │   ├── worker/      → Worker panel (dashboard, bookings, booking detail, profile)
│   │   ├── maid/        → Maid panel (dashboard, flat slots, add slot, profile)
│   │   └── auth/        → Login, Signup, Select Role, Create Profile, Pending, Rejected
│   ├── data/
│   │   ├── remote/      → Supabase DTOs
│   │   ├── local/       → Room DB (entities, DAOs)
│   │   └── repository/  → Repository implementations
│   └── di/              → Hilt modules
│
├── shared/src/commonMain/kotlin/com/mgbheights/shared/
│   ├── domain/
│   │   ├── model/       → User, Worker, WorkOrder, Visitor, Complaint, Notice, Payment, Flat, EditRequest
│   │   ├── repository/  → Repository interfaces
│   │   └── usecase/     → Business logic
│   └── util/            → Constants
│
├── iosApp/              → SwiftUI iOS app
└── webapp/              → Next.js web panel
```

---

*MGB Heights Society Management App — Keeping your community connected, managed, and secure.*
