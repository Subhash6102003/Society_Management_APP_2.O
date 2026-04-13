import 'package:supabase_flutter/supabase_flutter.dart';
import '../core/constants/app_constants.dart';
import '../core/demo/demo_mode.dart';
import '../models/user_model.dart';
import '../models/complaint_model.dart';
import '../models/notice_model.dart';
import '../models/visitor_model.dart';
import '../models/maintenance_bill_model.dart';
import '../models/payment_model.dart';
import '../models/worker_model.dart';
import '../models/maid_model.dart';
import '../models/shop_model.dart';

/// Central database service — wraps all Supabase table operations.
/// Each feature area has its own section for easy navigation.
class DatabaseService {
  DatabaseService._();
  static final DatabaseService instance = DatabaseService._();

  SupabaseClient get _db => Supabase.instance.client;

  // ════════════════════════════════════════════════════════════
  //  USERS
  // ════════════════════════════════════════════════════════════

  Future<List<UserModel>> getAllUsers() async {
    if (DemoMode.enabled) return DemoMode.allUsers;
    final rows = await _db.from(AppConstants.tableUsers).select().order('created_at', ascending: false);
    return (rows as List).map((r) => UserModel.fromJson(r)).toList();
  }

  Future<List<UserModel>> getUsersByRole(UserRole role) async {
    final rows = await _db
        .from(AppConstants.tableUsers)
        .select()
        .eq('role', role.name)
        .order('created_at', ascending: false);
    return (rows as List).map((r) => UserModel.fromJson(r)).toList();
  }

  Future<UserModel?> getUserById(String userId) async {
    final row = await _db
        .from(AppConstants.tableUsers)
        .select()
        .eq('id', userId)
        .maybeSingle();
    if (row == null) return null;
    return UserModel.fromJson(row);
  }

  Future<void> updateUserStatus({
    required String userId,
    ApprovalStatus? status,
    bool? isBlocked,
  }) async {
    final updates = <String, dynamic>{
      'updated_at': DateTime.now().millisecondsSinceEpoch,
    };
    if (status != null) updates['approval_status'] = status.name;
    if (isBlocked != null) updates['is_blocked'] = isBlocked;
    await _db.from(AppConstants.tableUsers).update(updates).eq('id', userId);
  }

  // ════════════════════════════════════════════════════════════
  //  NOTICES
  // ════════════════════════════════════════════════════════════

  Future<List<NoticeModel>> getNotices({String? roleFilter}) async {
    if (DemoMode.enabled) return DemoMode.notices;
    var query = _db.from(AppConstants.tableNotices).select();
    final rows = await query.order('is_pinned', ascending: false).order('created_at', ascending: false);
    return (rows as List).map((r) => NoticeModel.fromJson(r)).toList();
  }

  Future<NoticeModel> addNotice(NoticeModel notice) async {
    final row = await _db
        .from(AppConstants.tableNotices)
        .insert(notice.toJson())
        .select()
        .single();
    return NoticeModel.fromJson(row);
  }

  /// Convenience: create notice with named params.
  Future<void> createNotice({
    required String title,
    required String body,
    required NoticeCategory category,
    required String authorId,
    required String authorName,
  }) async {
    await _db.from(AppConstants.tableNotices).insert({
      'title': title,
      'content': body,
      'category': category.name,
      'author_id': authorId,
      'author_name': authorName,
      'is_pinned': false,
      'target_roles': <String>[],
      'created_at': DateTime.now().millisecondsSinceEpoch,
      'updated_at': DateTime.now().millisecondsSinceEpoch,
    });
  }

  Future<void> deleteNotice(String id) async {
    await _db.from(AppConstants.tableNotices).delete().eq('id', id);
  }

  // ════════════════════════════════════════════════════════════
  //  COMPLAINTS
  // ════════════════════════════════════════════════════════════

  Future<List<ComplaintModel>> getComplaints({String? userId}) async {
    if (DemoMode.enabled) {
      final all = DemoMode.complaints;
      return userId == null ? all : all.where((c) => c.userId == userId).toList();
    }
    var query = _db.from(AppConstants.tableComplaints).select();
    if (userId != null) {
      final rows = await query
          .eq('user_id', userId)
          .order('created_at', ascending: false);
      return (rows as List).map((r) => ComplaintModel.fromJson(r)).toList();
    }
    final rows = await query.order('created_at', ascending: false);
    return (rows as List).map((r) => ComplaintModel.fromJson(r)).toList();
  }

  Future<ComplaintModel> addComplaint(ComplaintModel complaint) async {
    final row = await _db
        .from(AppConstants.tableComplaints)
        .insert(complaint.toJson())
        .select()
        .single();
    return ComplaintModel.fromJson(row);
  }

  /// Convenience: create complaint with named params.
  Future<void> createComplaint({
    required String title,
    required String description,
    required ComplaintCategory category,
    required String residentId,
    required String residentName,
    String? flatNumber,
  }) async {
    await _db.from(AppConstants.tableComplaints).insert({
      'user_id': residentId,
      'user_name': residentName,
      'flat_number': flatNumber ?? '',
      'title': title,
      'description': description,
      'category': category.name,
      'status': ComplaintStatus.open.name,
      'image_urls': <String>[],
      'resolution': '',
      'created_at': DateTime.now().millisecondsSinceEpoch,
      'updated_at': DateTime.now().millisecondsSinceEpoch,
    });
  }

  Future<void> updateComplaintStatus({
    required String id,
    required ComplaintStatus status,
    String? resolution,
  }) async {
    final updates = <String, dynamic>{
      'status': status.name,
      'updated_at': DateTime.now().millisecondsSinceEpoch,
    };
    if (resolution != null) updates['admin_response'] = resolution;
    await _db.from(AppConstants.tableComplaints).update(updates).eq('id', id);
  }

  // ════════════════════════════════════════════════════════════
  //  VISITORS
  // ════════════════════════════════════════════════════════════

  Future<List<VisitorModel>> getVisitors({String? residentId}) async {
    if (DemoMode.enabled) {
      final all = DemoMode.visitors;
      return residentId == null ? all : all.where((v) => v.residentId == residentId).toList();
    }
    var query = _db.from(AppConstants.tableVisitors).select();
    if (residentId != null) {
      final rows = await query
          .eq('resident_id', residentId)
          .order('created_at', ascending: false);
      return (rows as List).map((r) => VisitorModel.fromJson(r)).toList();
    }
    final rows = await query.order('created_at', ascending: false);
    return (rows as List).map((r) => VisitorModel.fromJson(r)).toList();
  }

  Future<VisitorModel> addVisitor(VisitorModel visitor) async {
    final row = await _db
        .from(AppConstants.tableVisitors)
        .insert(visitor.toJson())
        .select()
        .single();
    return VisitorModel.fromJson(row);
  }

  /// Convenience: create visitor entry with named params.
  Future<void> createVisitor({
    required String residentId,
    required String flatNumber,
    required String name,
    String? phoneNumber,
    String? purpose,
    bool guardEntry = false,
  }) async {
    await _db.from(AppConstants.tableVisitors).insert({
      'resident_id': residentId,
      'flat_number': flatNumber,
      'visitor_name': name,
      'visitor_phone': phoneNumber ?? '',
      'purpose': purpose ?? '',
      'status': guardEntry ? VisitorStatus.checkedIn.name : VisitorStatus.pending.name,
      'entry_time': guardEntry ? DateTime.now().millisecondsSinceEpoch : 0,
      'exit_time': 0,
      'created_at': DateTime.now().millisecondsSinceEpoch,
      'updated_at': DateTime.now().millisecondsSinceEpoch,
    });
  }

  Future<void> updateVisitorStatus({
    required String id,
    required VisitorStatus status,
    int? entryTime,
    int? exitTime,
  }) async {
    final updates = <String, dynamic>{
      'status': status.name,
      'updated_at': DateTime.now().millisecondsSinceEpoch,
    };
    if (entryTime != null) updates['entry_time'] = entryTime;
    if (exitTime != null) updates['exit_time'] = exitTime;
    await _db.from(AppConstants.tableVisitors).update(updates).eq('id', id);
  }

  // ════════════════════════════════════════════════════════════
  //  MAINTENANCE BILLS
  // ════════════════════════════════════════════════════════════

  Future<List<MaintenanceBillModel>> getBills({String? residentId}) async {
    if (DemoMode.enabled) {
      final all = DemoMode.bills;
      return residentId == null ? all : all.where((b) => b.residentId == residentId).toList();
    }
    var query = _db.from(AppConstants.tableMaintenanceBills).select();
    if (residentId != null) {
      final rows = await query
          .eq('resident_id', residentId)
          .order('created_at', ascending: false);
      return (rows as List).map((r) => MaintenanceBillModel.fromJson(r)).toList();
    }
    final rows = await query.order('created_at', ascending: false);
    return (rows as List).map((r) => MaintenanceBillModel.fromJson(r)).toList();
  }

  Future<MaintenanceBillModel> createBill(MaintenanceBillModel bill) async {
    final row = await _db
        .from(AppConstants.tableMaintenanceBills)
        .insert(bill.toJson())
        .select()
        .single();
    return MaintenanceBillModel.fromJson(row);
  }

  /// Create a maintenance bill for a specific user.
  Future<void> createMaintenanceBill({
    required String userId,
    required String flatNumber,
    required String title,
    required double amount,
    required DateTime dueDate,
    String? description,
  }) async {
    final now = DateTime.now();
    await _db.from(AppConstants.tableMaintenanceBills).insert({
      'resident_id': userId,
      'flat_number': flatNumber,
      'title': title,
      'description': description ?? '',
      'amount': amount,
      'late_fee': 0.0,
      'total_amount': amount,
      'due_date': dueDate.millisecondsSinceEpoch,
      'status': BillStatus.pending.name,
      'month': '${now.year}-${now.month.toString().padLeft(2, '0')}',
      'year': now.year,
      'paid_at': 0,
      'created_at': now.millisecondsSinceEpoch,
      'updated_at': now.millisecondsSinceEpoch,
    });
  }

  /// Create maintenance bills for all users in a specific flat.
  Future<void> createMaintenanceBillForFlat({
    required String flatNumber,
    required String title,
    required double amount,
    required DateTime dueDate,
    String? description,
  }) async {
    // Look up users with this flatNumber
    final rows = await _db
        .from(AppConstants.tableUsers)
        .select()
        .eq('flat_number', flatNumber);
    final users = (rows as List).map((r) => UserModel.fromJson(r)).toList();
    for (final user in users) {
      await createMaintenanceBill(
        userId: user.id,
        flatNumber: flatNumber,
        title: title,
        amount: amount,
        dueDate: dueDate,
        description: description,
      );
    }
    // If no users found, create a bill with just the flat number
    if (users.isEmpty) {
      await createMaintenanceBill(
        userId: '',
        flatNumber: flatNumber,
        title: title,
        amount: amount,
        dueDate: dueDate,
        description: description,
      );
    }
  }

  Future<void> updateBillStatus({
    required String id,
    required BillStatus status,
    int? paidAt,
  }) async {
    final updates = <String, dynamic>{
      'status': status.name,
      'updated_at': DateTime.now().millisecondsSinceEpoch,
    };
    if (paidAt != null) updates['paid_at'] = paidAt;
    await _db.from(AppConstants.tableMaintenanceBills).update(updates).eq('id', id);
  }

  // ════════════════════════════════════════════════════════════
  //  PAYMENTS
  // ════════════════════════════════════════════════════════════

  Future<List<PaymentModel>> getPayments({String? userId}) async {
    if (DemoMode.enabled) {
      final all = DemoMode.payments;
      return userId == null ? all : all.where((p) => p.userId == userId).toList();
    }
    var query = _db.from(AppConstants.tablePayments).select();
    if (userId != null) {
      final rows = await query
          .eq('user_id', userId)
          .order('created_at', ascending: false);
      return (rows as List).map((r) => PaymentModel.fromJson(r)).toList();
    }
    final rows = await query.order('created_at', ascending: false);
    return (rows as List).map((r) => PaymentModel.fromJson(r)).toList();
  }

  Future<PaymentModel> createPayment(PaymentModel payment) async {
    final row = await _db
        .from(AppConstants.tablePayments)
        .insert(payment.toJson())
        .select()
        .single();
    return PaymentModel.fromJson(row);
  }

  // ════════════════════════════════════════════════════════════
  //  WORK ORDERS
  // ════════════════════════════════════════════════════════════

  Future<List<WorkOrderModel>> getWorkOrders({
    String? workerId,
    String? residentId,
  }) async {
    if (DemoMode.enabled) {
      final all = DemoMode.workOrders;
      if (workerId != null) return all.where((o) => o.workerId == workerId).toList();
      if (residentId != null) return all.where((o) => o.residentId == residentId).toList();
      return all;
    }
    var query = _db.from(AppConstants.tableWorkOrders).select();
    if (workerId != null) {
      final rows = await query
          .eq('worker_id', workerId)
          .order('created_at', ascending: false);
      return (rows as List).map((r) => WorkOrderModel.fromJson(r)).toList();
    }
    if (residentId != null) {
      final rows = await query
          .eq('resident_id', residentId)
          .order('created_at', ascending: false);
      return (rows as List).map((r) => WorkOrderModel.fromJson(r)).toList();
    }
    final rows = await query.order('created_at', ascending: false);
    return (rows as List).map((r) => WorkOrderModel.fromJson(r)).toList();
  }

  Future<WorkOrderModel> addWorkOrder(WorkOrderModel order) async {
    final row = await _db
        .from(AppConstants.tableWorkOrders)
        .insert(order.toJson())
        .select()
        .single();
    return WorkOrderModel.fromJson(row);
  }

  /// Convenience: create work order with named params.
  Future<void> createWorkOrder({
    required String title,
    required String description,
    required WorkerCategory category,
    required String residentId,
    required String residentName,
    String? flatNumber,
  }) async {
    await _db.from(AppConstants.tableWorkOrders).insert({
      'title': title,
      'description': description,
      'category': category.name,
      'resident_id': residentId,
      'resident_name': residentName,
      'flat_number': flatNumber ?? '',
      'status': WorkOrderStatus.pending.name,
      'amount': 0.0,
      'is_paid': false,
      'rating': 0.0,
      'feedback': '',
      'created_at': DateTime.now().millisecondsSinceEpoch,
      'updated_at': DateTime.now().millisecondsSinceEpoch,
    });
  }

  Future<void> updateWorkOrderStatus({
    required String id,
    required WorkOrderStatus status,
    int? startedAt,
    int? completedAt,
  }) async {
    final updates = <String, dynamic>{
      'status': status.name,
      'updated_at': DateTime.now().millisecondsSinceEpoch,
    };
    if (startedAt != null) updates['started_at'] = startedAt;
    if (completedAt != null) updates['completed_at'] = completedAt;
    await _db.from(AppConstants.tableWorkOrders).update(updates).eq('id', id);
  }

  Future<void> rateWorkOrder({
    required String id,
    required double rating,
    required String feedback,
  }) async {
    await _db.from(AppConstants.tableWorkOrders).update({
      'rating': rating,
      'feedback': feedback,
      'updated_at': DateTime.now().millisecondsSinceEpoch,
    }).eq('id', id);
  }

  // ════════════════════════════════════════════════════════════
  //  MAID ATTENDANCE
  // ════════════════════════════════════════════════════════════

  Future<List<MaidAttendanceModel>> getMaidAttendance({
    required String maidId,
  }) async {
    final rows = await _db
        .from(AppConstants.tableMaidAttendance)
        .select()
        .eq('maid_id', maidId)
        .order('date', ascending: false);
    return (rows as List).map((r) => MaidAttendanceModel.fromJson(r)).toList();
  }

  Future<MaidAttendanceModel> toggleMaidDuty({
    required String maidId,
    required bool isDutyOn,
    String maidName = '',
    String flatNumber = '',
    String towerBlock = '',
  }) async {
    final now = DateTime.now().millisecondsSinceEpoch;

    if (isDutyOn) {
      // Start duty
      final row = await _db
          .from(AppConstants.tableMaidAttendance)
          .insert({
            'maid_id': maidId,
            'maid_name': maidName,
            'flat_number': flatNumber,
            'tower_block': towerBlock,
            'is_duty_on': true,
            'duty_on_time': now,
            'date': now,
            'created_at': now,
          })
          .select()
          .single();
      return MaidAttendanceModel.fromJson(row);
    } else {
      // End duty — find today's open record and update
      final today = DateTime.fromMillisecondsSinceEpoch(now);
      final startOfDay = DateTime(today.year, today.month, today.day).millisecondsSinceEpoch;

      final rows = await _db
          .from(AppConstants.tableMaidAttendance)
          .select()
          .eq('maid_id', maidId)
          .eq('is_duty_on', true)
          .gte('duty_on_time', startOfDay)
          .order('duty_on_time', ascending: false)
          .limit(1);

      if ((rows as List).isEmpty) {
        throw Exception('No active duty found to end.');
      }

      final recordId = rows.first['id'] as String;
      final updated = await _db
          .from(AppConstants.tableMaidAttendance)
          .update({
            'is_duty_on': false,
            'duty_off_time': now,
          })
          .eq('id', recordId)
          .select()
          .single();
      return MaidAttendanceModel.fromJson(updated);
    }
  }

  // ════════════════════════════════════════════════════════════
  //  SHOP LISTINGS
  // ════════════════════════════════════════════════════════════

  /// Returns all active (non-sold, non-expired) listings, newest first.
  Future<List<ShopListingModel>> getActiveShopListings({
    ShopCategory? category,
    bool? freeOnly,
  }) async {
    var query = _db
        .from(AppConstants.tableShopListings)
        .select()
        .eq('is_available', true)
        .eq('is_sold', false)
        .gt('expires_at', DateTime.now().millisecondsSinceEpoch);
    if (freeOnly == true) {
      query = query.eq('is_free', true);
    }
    if (category != null) {
      query = query.eq('category', category.name);
    }
    final rows = await query.order('created_at', ascending: false);
    return (rows as List).map((r) => ShopListingModel.fromJson(r)).toList();
  }

  /// Returns all listings posted by [sellerId] (including sold/expired).
  Future<List<ShopListingModel>> getMyShopListings(String sellerId) async {
    final rows = await _db
        .from(AppConstants.tableShopListings)
        .select()
        .eq('seller_id', sellerId)
        .order('created_at', ascending: false);
    return (rows as List).map((r) => ShopListingModel.fromJson(r)).toList();
  }

  /// Inserts a new shop listing and returns the saved model with its generated id.
  Future<ShopListingModel> addShopListing(ShopListingModel listing) async {
    final now = DateTime.now().millisecondsSinceEpoch;
    final expiresAt = now + (AppConstants.shopExpireDays * 24 * 60 * 60 * 1000);
    final json = listing.toJson()
      ..remove('id') // let db generate
      ..['created_at'] = now
      ..['updated_at'] = now
      ..['expires_at'] = expiresAt;
    final row = await _db
        .from(AppConstants.tableShopListings)
        .insert(json)
        .select()
        .single();
    return ShopListingModel.fromJson(row);
  }

  /// Marks a listing as sold and unavailable.
  Future<void> markShopItemSold(String listingId) async {
    await _db.from(AppConstants.tableShopListings).update({
      'is_sold': true,
      'is_available': false,
      'updated_at': DateTime.now().millisecondsSinceEpoch,
    }).eq('id', listingId);
  }

  /// Deletes a listing (only the seller or admin should call this).
  Future<void> deleteShopListing(String listingId) async {
    await _db
        .from(AppConstants.tableShopListings)
        .delete()
        .eq('id', listingId);
  }
}
