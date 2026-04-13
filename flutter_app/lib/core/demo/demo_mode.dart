import '../../models/complaint_model.dart';
import '../../models/maintenance_bill_model.dart';
import '../../models/maid_model.dart';
import '../../models/notice_model.dart';
import '../../models/payment_model.dart';
import '../../models/user_model.dart';
import '../../models/visitor_model.dart';
import '../../models/worker_model.dart';

/// Global toggle. Set to true when Supabase is unavailable or user picks demo.
class DemoMode {
  DemoMode._();

  static bool enabled = false;

  // ── Demo credentials shown on login screen ──────────────────────────────────
  static const List<DemoCredential> credentials = [
    DemoCredential(role: UserRole.admin,         label: 'Admin',    email: 'admin@mgbheights.demo',    password: 'demo1234'),
    DemoCredential(role: UserRole.resident,      label: 'Resident', email: 'resident@mgbheights.demo', password: 'demo1234'),
    DemoCredential(role: UserRole.tenant,        label: 'Tenant',   email: 'tenant@mgbheights.demo',   password: 'demo1234'),
    DemoCredential(role: UserRole.securityGuard, label: 'Guard',    email: 'guard@mgbheights.demo',    password: 'demo1234'),
    DemoCredential(role: UserRole.worker,        label: 'Worker',   email: 'worker@mgbheights.demo',   password: 'demo1234'),
    DemoCredential(role: UserRole.maid,          label: 'Maid',     email: 'maid@mgbheights.demo',     password: 'demo1234'),
  ];

  // ── Demo Users ──────────────────────────────────────────────────────────────

  static UserModel userForRole(UserRole role) {
    final now = DateTime.now().millisecondsSinceEpoch;
    switch (role) {
      case UserRole.admin:
        return UserModel(
          id: 'demo-admin-001',
          email: 'admin@mgbheights.demo',
          name: 'Rajesh Sharma',
          role: UserRole.admin,
          approvalStatus: ApprovalStatus.approved,
          isProfileComplete: true,
          phoneNumber: '9876543210',
          flatNumber: 'ADMIN',
          createdAt: now,
          updatedAt: now,
        );
      case UserRole.resident:
        return UserModel(
          id: 'demo-resident-001',
          email: 'resident@mgbheights.demo',
          name: 'Priya Mehta',
          role: UserRole.resident,
          approvalStatus: ApprovalStatus.approved,
          isProfileComplete: true,
          phoneNumber: '9876543211',
          flatNumber: 'A-304',
          towerBlock: 'A',
          createdAt: now,
          updatedAt: now,
        );
      case UserRole.tenant:
        return UserModel(
          id: 'demo-tenant-001',
          email: 'tenant@mgbheights.demo',
          name: 'Arjun Nair',
          role: UserRole.tenant,
          approvalStatus: ApprovalStatus.approved,
          isProfileComplete: true,
          phoneNumber: '9876543212',
          flatNumber: 'B-102',
          towerBlock: 'B',
          createdAt: now,
          updatedAt: now,
        );
      case UserRole.securityGuard:
        return UserModel(
          id: 'demo-guard-001',
          email: 'guard@mgbheights.demo',
          name: 'Ramesh Kumar',
          role: UserRole.securityGuard,
          approvalStatus: ApprovalStatus.approved,
          isProfileComplete: true,
          phoneNumber: '9876543213',
          flatNumber: 'GATE-1',
          createdAt: now,
          updatedAt: now,
        );
      case UserRole.worker:
        return UserModel(
          id: 'demo-worker-001',
          email: 'worker@mgbheights.demo',
          name: 'Suresh Patel',
          role: UserRole.worker,
          approvalStatus: ApprovalStatus.approved,
          isProfileComplete: true,
          phoneNumber: '9876543214',
          flatNumber: '',
          createdAt: now,
          updatedAt: now,
        );
      case UserRole.maid:
        return UserModel(
          id: 'demo-maid-001',
          email: 'maid@mgbheights.demo',
          name: 'Sunita Devi',
          role: UserRole.maid,
          approvalStatus: ApprovalStatus.approved,
          isProfileComplete: true,
          phoneNumber: '9876543215',
          flatNumber: '',
          createdAt: now,
          updatedAt: now,
        );
    }
  }

  // ── Demo Data ───────────────────────────────────────────────────────────────

  static List<UserModel> get allUsers => UserRole.values.map(userForRole).toList();

  static List<NoticeModel> get notices {
    final now = DateTime.now().millisecondsSinceEpoch;
    return [
      NoticeModel(
        id: 'n1', title: 'Water Supply Shut-off',
        body: 'Water supply will be shut off on 12th April from 10am to 2pm for maintenance.',
        createdAt: now - 86400000, updatedAt: now - 86400000,
        createdBy: 'demo-admin-001',
      ),
      NoticeModel(
        id: 'n2', title: 'Society Meeting Notice',
        body: 'Monthly society meeting on 15th April at 7pm in the Community Hall.',
        createdAt: now - 172800000, updatedAt: now - 172800000,
        createdBy: 'demo-admin-001',
      ),
      NoticeModel(
        id: 'n3', title: 'Fit India Campaign',
        body: 'Join us for the annual Fit India Run on 20th April at 6am from the main gate.',
        createdAt: now - 259200000, updatedAt: now - 259200000,
        createdBy: 'demo-admin-001',
      ),
    ];
  }

  static List<MaintenanceBillModel> get bills {
    final now = DateTime.now().millisecondsSinceEpoch;
    final nextMonth = now + 30 * 86400000;
    return [
      MaintenanceBillModel(
        id: 'b1', title: 'April Maintenance',
        description: 'Monthly maintenance charge for April 2026',
        amount: 2500, residentId: 'demo-resident-001',
        dueDate: nextMonth, status: BillStatus.pending,
        createdAt: now, updatedAt: now,
      ),
      MaintenanceBillModel(
        id: 'b2', title: 'Water Charges',
        description: 'Water usage charges for March 2026',
        amount: 800, residentId: 'demo-resident-001',
        dueDate: now - 5 * 86400000, status: BillStatus.overdue,
        createdAt: now - 30 * 86400000, updatedAt: now,
      ),
      MaintenanceBillModel(
        id: 'b3', title: 'March Maintenance',
        description: 'Monthly maintenance charge for March 2026',
        amount: 2500, residentId: 'demo-resident-001',
        dueDate: now - 15 * 86400000, status: BillStatus.paid,
        createdAt: now - 60 * 86400000, updatedAt: now - 10 * 86400000,
      ),
      // Tenant bills
      MaintenanceBillModel(
        id: 'b4', title: 'April Rent',
        description: 'Rent for April 2026',
        amount: 18000, residentId: 'demo-tenant-001',
        dueDate: nextMonth, status: BillStatus.pending,
        createdAt: now, updatedAt: now,
      ),
    ];
  }

  static List<ComplaintModel> get complaints {
    final now = DateTime.now().millisecondsSinceEpoch;
    return [
      ComplaintModel(
        id: 'c1', title: 'Water Leakage',
        description: 'There is a water leakage in the corridor near flat A-304.',
        userId: 'demo-resident-001', userName: 'Priya Mehta',
        status: ComplaintStatus.open,
        createdAt: now - 3 * 86400000, updatedAt: now - 3 * 86400000,
        resolution: '',
      ),
      ComplaintModel(
        id: 'c2', title: 'Lift Not Working',
        description: 'The elevator in Tower A is out of service since yesterday.',
        userId: 'demo-resident-001', userName: 'Priya Mehta',
        status: ComplaintStatus.inProgress,
        createdAt: now - 2 * 86400000, updatedAt: now - 86400000,
        resolution: '',
      ),
      ComplaintModel(
        id: 'c3', title: 'Parking Dispute',
        description: 'Someone is parking in my designated spot daily.',
        userId: 'demo-tenant-001', userName: 'Arjun Nair',
        status: ComplaintStatus.resolved,
        createdAt: now - 7 * 86400000, updatedAt: now - 2 * 86400000,
        resolution: 'Management has issued a warning to the vehicle owner.',
      ),
    ];
  }

  static List<VisitorModel> get visitors {
    final now = DateTime.now().millisecondsSinceEpoch;
    return [
      VisitorModel(
        id: 'v1', name: 'Deepak Gupta',
        phoneNumber: '9123456789', purpose: 'Personal Visit',
        residentId: 'demo-resident-001', residentName: 'Priya Mehta',
        flatNumber: 'A-304', status: VisitorStatus.checkedIn,
        entryTime: now - 3600000,
        createdAt: now - 3600000,
      ),
      VisitorModel(
        id: 'v2', name: 'Kavita Singh',
        phoneNumber: '9123456790', purpose: 'Parcel Delivery',
        residentId: 'demo-resident-001', residentName: 'Priya Mehta',
        flatNumber: 'A-304', status: VisitorStatus.pending,
        entryTime: now,
        createdAt: now,
      ),
      VisitorModel(
        id: 'v3', name: 'Mohan Das',
        phoneNumber: '9123456791', purpose: 'Electrician',
        residentId: 'demo-tenant-001', residentName: 'Arjun Nair',
        flatNumber: 'B-102', status: VisitorStatus.checkedOut,
        entryTime: now - 7200000, exitTime: now - 3000000,
        createdAt: now - 7200000,
      ),
    ];
  }

  static List<WorkOrderModel> get workOrders {
    final now = DateTime.now().millisecondsSinceEpoch;
    return [
      WorkOrderModel(
        id: 'wo1', title: 'Fix Water Leakage',
        description: 'Repair the water pipe leak in Tower A corridor.',
        workerId: 'demo-worker-001', residentId: 'demo-resident-001',
        status: WorkOrderStatus.pending, category: WorkerCategory.plumber,
        scheduledAt: now + 86400000,
        createdAt: now, updatedAt: now,
      ),
      WorkOrderModel(
        id: 'wo2', title: 'Electrical Wiring Check',
        description: 'Inspect main panel wiring in Block B.',
        workerId: 'demo-worker-001', residentId: 'demo-admin-001',
        status: WorkOrderStatus.accepted, category: WorkerCategory.electrician,
        scheduledAt: now + 2 * 86400000,
        createdAt: now - 86400000, updatedAt: now,
      ),
      WorkOrderModel(
        id: 'wo3', title: 'Paint Common Area',
        description: 'Touch-up paint on ground floor lobby walls.',
        workerId: 'demo-worker-001', residentId: 'demo-admin-001',
        status: WorkOrderStatus.completed, category: WorkerCategory.painter,
        scheduledAt: now - 3 * 86400000,
        createdAt: now - 7 * 86400000, updatedAt: now - 3 * 86400000,
      ),
    ];
  }

  static List<PaymentModel> get payments {
    final now = DateTime.now().millisecondsSinceEpoch;
    return [
      PaymentModel(
        id: 'p1', userId: 'demo-resident-001', billId: 'b3',
        amount: 2500, status: PaymentStatus.success,
        type: PaymentType.maintenance,
        note: 'Paid via bank transfer',
        paidAt: DateTime.now().millisecondsSinceEpoch - 10 * 86400000,
        createdAt: now - 10 * 86400000,
      ),
    ];
  }

  static List<MaidSlotModel> get maids {
    final now = DateTime.now().millisecondsSinceEpoch;
    return [
      MaidSlotModel(
        id: 'demo-maid-slot-001',
        maidId: 'demo-maid-001',
        maidName: 'Sunita Devi',
        flatNumber: 'A-304',
        towerBlock: 'A',
        residentId: 'demo-resident-001',
        timeSlot: '08:00 AM - 09:00 AM',
        days: ['MON', 'WED', 'FRI'],
        isActive: true,
        createdAt: now, updatedAt: now,
      ),
    ];
  }
}

class DemoCredential {
  final UserRole role;
  final String label;
  final String email;
  final String password;
  const DemoCredential({
    required this.role,
    required this.label,
    required this.email,
    required this.password,
  });
}
