import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:supabase_flutter/supabase_flutter.dart';
import '../models/user_model.dart';
import '../services/database_service.dart';
import '../core/constants/app_constants.dart';

// ─── Admin: all users ─────────────────────────────────────────────────────────

final allUsersProvider = FutureProvider<List<UserModel>>((ref) async {
  return DatabaseService.instance.getAllUsers();
});

final usersByRoleProvider = FutureProvider.family<List<UserModel>, UserRole>(
  (ref, role) async {
    return DatabaseService.instance.getUsersByRole(role);
  },
);

final approvalPendingUsersProvider = FutureProvider<List<UserModel>>((ref) async {
  final all = await DatabaseService.instance.getAllUsers();
  // Show all users with pending approval — including those still completing profile
  return all
      .where((u) => u.approvalStatus == ApprovalStatus.pending)
      .toList();
});

/// Realtime stream of pending approval users — admin panel auto-updates when
/// a new user signs up without needing a manual refresh.
final pendingUsersStreamProvider = StreamProvider<List<UserModel>>((ref) {
  final client = Supabase.instance.client;

  final stream = client
      .from(AppConstants.tableUsers)
      .stream(primaryKey: ['id'])
      .eq('approval_status', 'pending')
      .order('created_at', ascending: false)
      .map((rows) => rows.map((r) => UserModel.fromJson(r)).toList());

  return stream;
});

