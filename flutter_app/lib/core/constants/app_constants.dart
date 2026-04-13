import 'package:flutter_dotenv/flutter_dotenv.dart';

/// Central configuration constants for MGB Heights Flutter app.
class AppConstants {
  AppConstants._();

  // ── Supabase ────────────────────────────────────────────────────────────────
  static String get supabaseUrl =>
      dotenv.env['SUPABASE_URL'] ?? (throw Exception('SUPABASE_URL not set in .env'));
  static String get supabaseAnonKey =>
      dotenv.env['SUPABASE_ANON_KEY'] ?? (throw Exception('SUPABASE_ANON_KEY not set in .env'));


  // ── Supabase table names ────────────────────────────────────────────────────
  static const String tableUsers = 'users';
  static const String tableFlats = 'flats';
  static const String tableNotices = 'notices';
  static const String tableComplaints = 'complaints';
  static const String tableVisitors = 'visitors';
  static const String tableMaintenanceBills = 'maintenance_bills';
  static const String tablePayments = 'payments';
  static const String tableWorkOrders = 'work_orders';
  static const String tableWorkers = 'workers';
  static const String tableMaidSlots = 'maid_slots';
  static const String tableMaidAttendance = 'maid_attendance';
  static const String tableShopListings = 'shop_listings';
  static const String tableEditRequests = 'edit_requests';

  // ── Supabase storage buckets ────────────────────────────────────────────────
  static const String bucketProfiles = 'profiles';
  static const String bucketIdProofs = 'id-proofs';
  static const String bucketVisitors = 'visitors';
  static const String bucketComplaints = 'complaints';
  static const String bucketNotices = 'notices';
  static const String bucketShop = 'shop';

  // ── App-level constants ─────────────────────────────────────────────────────
  static const String appName = 'MGB Heights';
  static const int visitorAutoExpiryMinutes = 30;
  static const int otpResendCooldownSeconds = 60;
  static const int shopExpireDays = 15;
}
