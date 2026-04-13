import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../providers/auth_provider.dart';
import '../models/user_model.dart';

// ── Auth screens
import '../features/auth/screens/splash_screen.dart';
import '../features/auth/screens/landing_screen.dart';
import '../features/auth/screens/login_screen.dart';
import '../features/auth/screens/signup_screen.dart';
import '../features/auth/screens/select_role_screen.dart';
import '../features/auth/screens/create_profile_screen.dart';
import '../features/auth/screens/pending_approval_screen.dart';
import '../features/auth/screens/rejected_screen.dart';

// ── Admin screens
import '../features/admin/screens/admin_shell.dart';
import '../features/admin/screens/admin_dashboard_screen.dart';
import '../features/admin/screens/admin_user_management_screen.dart';
import '../features/admin/screens/admin_notices_screen.dart';
import '../features/admin/screens/admin_complaints_screen.dart';
import '../features/admin/screens/admin_visitors_screen.dart';
import '../features/admin/screens/admin_bills_screen.dart';
import '../features/admin/screens/admin_profile_screen.dart';

// ── Resident screens
import '../features/resident/screens/resident_shell.dart';
import '../features/resident/screens/resident_home_screen.dart';
import '../features/resident/screens/resident_bills_screen.dart';
import '../features/resident/screens/resident_complaints_screen.dart';
import '../features/resident/screens/resident_notices_screen.dart';
import '../features/resident/screens/resident_visitors_screen.dart';
import '../features/resident/screens/resident_workers_screen.dart';
import '../features/resident/screens/resident_maid_screen.dart';
import '../features/resident/screens/resident_profile_screen.dart';

// ── Tenant screens
import '../features/tenant/screens/tenant_shell.dart';
import '../features/tenant/screens/tenant_home_screen.dart';
import '../features/tenant/screens/tenant_bills_screen.dart';
import '../features/tenant/screens/tenant_complaints_screen.dart';
import '../features/tenant/screens/tenant_notices_screen.dart';
import '../features/tenant/screens/tenant_profile_screen.dart';

// ── Guard screens
import '../features/guard/screens/guard_shell.dart';
import '../features/guard/screens/guard_dashboard_screen.dart';
import '../features/guard/screens/guard_add_visitor_screen.dart';
import '../features/guard/screens/guard_visitor_log_screen.dart';
import '../features/guard/screens/guard_profile_screen.dart';

// ── Worker screens
import '../features/worker/screens/worker_shell.dart';
import '../features/worker/screens/worker_dashboard_screen.dart';
import '../features/worker/screens/worker_bookings_screen.dart';
import '../features/worker/screens/worker_earnings_screen.dart';
import '../features/worker/screens/worker_profile_screen.dart';

// ── Maid screens
import '../features/maid/screens/maid_shell.dart';
import '../features/maid/screens/maid_dashboard_screen.dart';
import '../features/maid/screens/maid_attendance_screen.dart';
import '../features/maid/screens/maid_profile_screen.dart';

// ── Shop screens
import '../features/shop/screens/shop_screen.dart';
import '../features/shop/screens/add_shop_item_screen.dart';

// ─── Route names (use these constants everywhere instead of raw strings) ────────

class AppRoutes {
  AppRoutes._();
  static const String splash = '/';
  static const String landing = '/landing';
  static const String login = '/login';
  static const String signup = '/signup';
  static const String selectRole = '/select-role';
  static const String createProfile = '/create-profile';
  static const String pendingApproval = '/pending';
  static const String rejected = '/rejected';

  // Admin
  static const String admin = '/admin';
  static const String adminDashboard = '/admin/dashboard';
  static const String adminUsers = '/admin/users';
  static const String adminNotices = '/admin/notices';
  static const String adminComplaints = '/admin/complaints';
  static const String adminVisitors = '/admin/visitors';
  static const String adminBills = '/admin/bills';
  static const String adminProfile = '/admin/profile';

  // Resident
  static const String resident = '/resident';
  static const String residentHome = '/resident/home';
  static const String residentBills = '/resident/bills';
  static const String residentComplaints = '/resident/complaints';
  static const String residentNotices = '/resident/notices';
  static const String residentVisitors = '/resident/visitors';
  static const String residentWorkers = '/resident/workers';
  static const String residentMaid = '/resident/maid';
  static const String residentProfile = '/resident/profile';

  // Tenant
  static const String tenant = '/tenant';
  static const String tenantHome = '/tenant/home';
  static const String tenantBills = '/tenant/bills';
  static const String tenantComplaints = '/tenant/complaints';
  static const String tenantNotices = '/tenant/notices';
  static const String tenantProfile = '/tenant/profile';

  // Guard
  static const String guard = '/guard';
  static const String guardDashboard = '/guard/dashboard';
  static const String guardAddVisitor = '/guard/add-visitor';
  static const String guardVisitorLog = '/guard/visitor-log';
  static const String guardProfile = '/guard/profile';

  // Worker
  static const String worker = '/worker';
  static const String workerDashboard = '/worker/dashboard';
  static const String workerBookings = '/worker/bookings';
  static const String workerEarnings = '/worker/earnings';
  static const String workerProfile = '/worker/profile';

  // Maid
  static const String maid = '/maid';
  static const String maidDashboard = '/maid/dashboard';
  static const String maidAttendance = '/maid/attendance';
  static const String maidProfile = '/maid/profile';

  // Shop (shared across roles, one route per shell + standalone add)
  static const String residentShop = '/resident/shop';
  static const String tenantShop = '/tenant/shop';
  static const String adminShop = '/admin/shop';
  static const String guardShop = '/guard/shop';
  static const String workerShop = '/worker/shop';
  static const String maidShop = '/maid/shop';
  static const String shopAddItem = '/shop/add';
}

// ─── Router Provider ──────────────────────────────────────────────────────────

final appRouterProvider = Provider<GoRouter>((ref) {
  final authState = ref.watch(authProvider);

  return GoRouter(
    initialLocation: AppRoutes.splash,
    debugLogDiagnostics: true,
    redirect: (BuildContext context, GoRouterState state) {
      final status = authState.status;
      final loc = state.matchedLocation;

      // Allow splash to always show first
      if (loc == AppRoutes.splash) return null;

      switch (status) {
        case AuthStatus.loading:
          return AppRoutes.splash;

        case AuthStatus.unauthenticated:
          if (loc == AppRoutes.landing ||
              loc == AppRoutes.login ||
              loc == AppRoutes.signup ||
              loc == AppRoutes.selectRole) return null;
          return AppRoutes.landing;

        case AuthStatus.pendingProfile:
          if (loc == AppRoutes.createProfile) return null;
          return AppRoutes.createProfile;

        case AuthStatus.pending:
          if (loc == AppRoutes.pendingApproval) return null;
          return AppRoutes.pendingApproval;

        case AuthStatus.rejected:
          if (loc == AppRoutes.rejected) return null;
          return AppRoutes.rejected;

        case AuthStatus.blocked:
          return AppRoutes.landing;

        case AuthStatus.approved:
          // Already in the right area — no redirect needed
          if (_isRoleRoute(loc, authState.user?.role)) return null;
          return _dashboardForRole(authState.user?.role);
      }
    },
    routes: [
      // ── Splash
      GoRoute(path: AppRoutes.splash, builder: (_, __) => const SplashScreen()),

      // ── Auth
      GoRoute(path: AppRoutes.landing, builder: (_, __) => const LandingScreen()),
      GoRoute(path: AppRoutes.login, builder: (_, __) => const LoginScreen()),
      GoRoute(path: AppRoutes.signup, builder: (_, __) => const SignupScreen()),
      GoRoute(path: AppRoutes.selectRole, builder: (_, __) => const SelectRoleScreen()),
      GoRoute(path: AppRoutes.createProfile, builder: (_, __) => const CreateProfileScreen()),
      GoRoute(path: AppRoutes.pendingApproval, builder: (_, __) => const PendingApprovalScreen()),
      GoRoute(path: AppRoutes.rejected, builder: (_, __) => const RejectedScreen()),

      // ── Admin shell with nested tabs
      ShellRoute(
        builder: (_, __, child) => AdminShell(child: child),
        routes: [
          GoRoute(path: AppRoutes.adminDashboard, builder: (_, __) => const AdminDashboardScreen()),
          GoRoute(path: AppRoutes.adminUsers, builder: (_, __) => const AdminUserManagementScreen()),
          GoRoute(path: AppRoutes.adminNotices, builder: (_, __) => const AdminNoticesScreen()),
          GoRoute(path: AppRoutes.adminComplaints, builder: (_, __) => const AdminComplaintsScreen()),
          GoRoute(path: AppRoutes.adminVisitors, builder: (_, __) => const AdminVisitorsScreen()),
          GoRoute(path: AppRoutes.adminBills, builder: (_, __) => const AdminBillsScreen()),
          GoRoute(path: AppRoutes.adminProfile, builder: (_, __) => const AdminProfileScreen()),
          GoRoute(path: AppRoutes.adminShop, builder: (_, __) => const ShopScreen()),
        ],
      ),

      // ── Resident shell
      ShellRoute(
        builder: (_, __, child) => ResidentShell(child: child),
        routes: [
          GoRoute(path: AppRoutes.residentHome, builder: (_, __) => const ResidentHomeScreen()),
          GoRoute(path: AppRoutes.residentBills, builder: (_, __) => const ResidentBillsScreen()),
          GoRoute(path: AppRoutes.residentComplaints, builder: (_, __) => const ResidentComplaintsScreen()),
          GoRoute(path: AppRoutes.residentNotices, builder: (_, __) => const ResidentNoticesScreen()),
          GoRoute(path: AppRoutes.residentVisitors, builder: (_, __) => const ResidentVisitorsScreen()),
          GoRoute(path: AppRoutes.residentWorkers, builder: (_, __) => const ResidentWorkersScreen()),
          GoRoute(path: AppRoutes.residentMaid, builder: (_, __) => const ResidentMaidScreen()),
          GoRoute(path: AppRoutes.residentProfile, builder: (_, __) => const ResidentProfileScreen()),
          GoRoute(path: AppRoutes.residentShop, builder: (_, __) => const ShopScreen()),
        ],
      ),

      // ── Tenant shell
      ShellRoute(
        builder: (_, __, child) => TenantShell(child: child),
        routes: [
          GoRoute(path: AppRoutes.tenantHome, builder: (_, __) => const TenantHomeScreen()),
          GoRoute(path: AppRoutes.tenantBills, builder: (_, __) => const TenantBillsScreen()),
          GoRoute(path: AppRoutes.tenantComplaints, builder: (_, __) => const TenantComplaintsScreen()),
          GoRoute(path: AppRoutes.tenantNotices, builder: (_, __) => const TenantNoticesScreen()),
          GoRoute(path: AppRoutes.tenantProfile, builder: (_, __) => const TenantProfileScreen()),
          GoRoute(path: AppRoutes.tenantShop, builder: (_, __) => const ShopScreen()),
        ],
      ),

      // ── Guard shell
      ShellRoute(
        builder: (_, __, child) => GuardShell(child: child),
        routes: [
          GoRoute(path: AppRoutes.guardDashboard, builder: (_, __) => const GuardDashboardScreen()),
          GoRoute(path: AppRoutes.guardAddVisitor, builder: (_, __) => const GuardAddVisitorScreen()),
          GoRoute(path: AppRoutes.guardVisitorLog, builder: (_, __) => const GuardVisitorLogScreen()),
          GoRoute(path: AppRoutes.guardProfile, builder: (_, __) => const GuardProfileScreen()),
          GoRoute(path: AppRoutes.guardShop, builder: (_, __) => const ShopScreen()),
        ],
      ),

      // ── Worker shell
      ShellRoute(
        builder: (_, __, child) => WorkerShell(child: child),
        routes: [
          GoRoute(path: AppRoutes.workerDashboard, builder: (_, __) => const WorkerDashboardScreen()),
          GoRoute(path: AppRoutes.workerBookings, builder: (_, __) => const WorkerBookingsScreen()),
          GoRoute(path: AppRoutes.workerEarnings, builder: (_, __) => const WorkerEarningsScreen()),
          GoRoute(path: AppRoutes.workerProfile, builder: (_, __) => const WorkerProfileScreen()),
          GoRoute(path: AppRoutes.workerShop, builder: (_, __) => const ShopScreen()),
        ],
      ),

      // ── Maid shell
      ShellRoute(
        builder: (_, __, child) => MaidShell(child: child),
        routes: [
          GoRoute(path: AppRoutes.maidDashboard, builder: (_, __) => const MaidDashboardScreen()),
          GoRoute(path: AppRoutes.maidAttendance, builder: (_, __) => const MaidAttendanceScreen()),
          GoRoute(path: AppRoutes.maidProfile, builder: (_, __) => const MaidProfileScreen()),
          GoRoute(path: AppRoutes.maidShop, builder: (_, __) => const ShopScreen()),
        ],
      ),

      // ── Add shop item (push, any role)
      GoRoute(path: AppRoutes.shopAddItem, builder: (_, __) => const AddShopItemScreen()),
    ],
  );
});

// ─── Helpers ──────────────────────────────────────────────────────────────────

bool _isRoleRoute(String location, UserRole? role) {
  if (role == null) return false;
  final prefix = _dashboardForRole(role) ?? '';
  return location.startsWith(prefix.split('/').take(2).join('/'));
}

String? _dashboardForRole(UserRole? role) {
  switch (role) {
    case UserRole.admin:
      return AppRoutes.adminDashboard;
    case UserRole.resident:
      return AppRoutes.residentHome;
    case UserRole.tenant:
      return AppRoutes.tenantHome;
    case UserRole.securityGuard:
      return AppRoutes.guardDashboard;
    case UserRole.worker:
      return AppRoutes.workerDashboard;
    case UserRole.maid:
      return AppRoutes.maidDashboard;
    default:
      return AppRoutes.landing;
  }
}
