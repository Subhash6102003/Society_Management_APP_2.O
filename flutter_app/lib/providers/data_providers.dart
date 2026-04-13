import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../models/complaint_model.dart';
import '../models/notice_model.dart';
import '../models/visitor_model.dart';
import '../models/maintenance_bill_model.dart';
import '../models/payment_model.dart';
import '../models/worker_model.dart';
import '../models/maid_model.dart';
import '../services/database_service.dart';
import 'auth_provider.dart';

final _db = DatabaseService.instance;

// ─── Notices ─────────────────────────────────────────────────────────────────

final noticesProvider = FutureProvider<List<NoticeModel>>(
  (_) => _db.getNotices(),
);

// ─── Complaints ───────────────────────────────────────────────────────────────

final complaintsProvider = FutureProvider<List<ComplaintModel>>(
  (_) => _db.getComplaints(),
);

/// My complaints — scoped to current user
final myComplaintsProvider = FutureProvider<List<ComplaintModel>>((ref) async {
  final user = ref.watch(currentUserProvider);
  if (user == null) return [];
  return _db.getComplaints(userId: user.id);
});

// ─── Visitors ────────────────────────────────────────────────────────────────

final visitorsProvider = FutureProvider<List<VisitorModel>>(
  (_) => _db.getVisitors(),
);

final myVisitorsProvider = FutureProvider<List<VisitorModel>>((ref) async {
  final user = ref.watch(currentUserProvider);
  if (user == null) return [];
  return _db.getVisitors(residentId: user.id);
});

// ─── Bills ───────────────────────────────────────────────────────────────────

final allBillsProvider = FutureProvider<List<MaintenanceBillModel>>(
  (_) => _db.getBills(),
);

final myBillsProvider = FutureProvider<List<MaintenanceBillModel>>((ref) async {
  final user = ref.watch(currentUserProvider);
  if (user == null) return [];
  return _db.getBills(residentId: user.id);
});

// ─── Payments ────────────────────────────────────────────────────────────────

final allPaymentsProvider = FutureProvider<List<PaymentModel>>(
  (_) => _db.getPayments(),
);

final myPaymentsProvider = FutureProvider<List<PaymentModel>>((ref) async {
  final user = ref.watch(currentUserProvider);
  if (user == null) return [];
  return _db.getPayments(userId: user.id);
});

// ─── Work Orders ──────────────────────────────────────────────────────────────

final allWorkOrdersProvider = FutureProvider<List<WorkOrderModel>>(
  (_) => _db.getWorkOrders(),
);

final myWorkOrdersAsWorkerProvider = FutureProvider<List<WorkOrderModel>>((ref) async {
  final user = ref.watch(currentUserProvider);
  if (user == null) return [];
  return _db.getWorkOrders(workerId: user.id);
});

final myWorkOrdersAsResidentProvider = FutureProvider<List<WorkOrderModel>>((ref) async {
  final user = ref.watch(currentUserProvider);
  if (user == null) return [];
  return _db.getWorkOrders(residentId: user.id);
});

// ─── Maid Attendance ─────────────────────────────────────────────────────────

final myMaidAttendanceProvider = FutureProvider<List<MaidAttendanceModel>>((ref) async {
  final user = ref.watch(currentUserProvider);
  if (user == null) return [];
  return _db.getMaidAttendance(maidId: user.id);
});
