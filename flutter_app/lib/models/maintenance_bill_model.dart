// ─── Bill Status ─────────────────────────────────────────────────────────────

enum BillStatus {
  pending,
  paid,
  overdue,
  cancelled;

  String get displayName => switch (this) {
    BillStatus.pending => 'Pending',
    BillStatus.paid => 'Paid',
    BillStatus.overdue => 'Overdue',
    BillStatus.cancelled => 'Cancelled',
  };

  static BillStatus fromString(String value) => switch (value.toUpperCase()) {
    'PAID' => BillStatus.paid,
    'OVERDUE' => BillStatus.overdue,
    'CANCELLED' => BillStatus.cancelled,
    _ => BillStatus.pending,
  };
}

// ─── Maintenance Bill ─────────────────────────────────────────────────────────

class MaintenanceBillModel {
  final String id;
  final String flatId;
  final String flatNumber;
  final String towerBlock;
  final String residentId;
  final String residentName;
  final String title;
  final double amount;
  final double lateFee;
  final double totalAmount;
  final String month;
  final int year;
  final int dueDate;
  final BillStatus status;
  final String description;
  final int paidAt;
  final int createdAt;
  final int updatedAt;

  const MaintenanceBillModel({
    required this.id,
    this.flatId = '',
    this.flatNumber = '',
    this.towerBlock = '',
    this.residentId = '',
    this.residentName = '',
    this.title = '',
    this.amount = 0.0,
    this.lateFee = 0.0,
    this.totalAmount = 0.0,
    this.month = '',
    this.year = 0,
    this.dueDate = 0,
    this.status = BillStatus.pending,
    this.description = '',
    this.paidAt = 0,
    this.createdAt = 0,
    this.updatedAt = 0,
  });

  factory MaintenanceBillModel.fromJson(Map<String, dynamic> json) {
    return MaintenanceBillModel(
      id: json['id'] as String? ?? '',
      flatId: json['flat_id'] as String? ?? '',
      flatNumber: json['flat_number'] as String? ?? '',
      towerBlock: json['tower_block'] as String? ?? '',
      residentId: json['resident_id'] as String? ?? '',
      residentName: json['resident_name'] as String? ?? '',
      title: json['title'] as String? ?? '',
      amount: (json['amount'] as num?)?.toDouble() ?? 0.0,
      lateFee: (json['late_fee'] as num?)?.toDouble() ?? 0.0,
      totalAmount: (json['total_amount'] as num?)?.toDouble() ?? 0.0,
      month: json['month'] as String? ?? '',
      year: (json['year'] as num?)?.toInt() ?? 0,
      dueDate: (json['due_date'] as num?)?.toInt() ?? 0,
      status: BillStatus.fromString(json['status'] as String? ?? 'PENDING'),
      description: json['description'] as String? ?? '',
      paidAt: (json['paid_at'] as num?)?.toInt() ?? 0,
      createdAt: (json['created_at'] as num?)?.toInt() ?? 0,
      updatedAt: (json['updated_at'] as num?)?.toInt() ?? 0,
    );
  }

  Map<String, dynamic> toJson() => {
        'id': id,
        'flat_id': flatId,
        'flat_number': flatNumber,
        'tower_block': towerBlock,
        'resident_id': residentId,
        'resident_name': residentName,
        'title': title,
        'amount': amount,
        'late_fee': lateFee,
        'total_amount': totalAmount,
        'month': month,
        'year': year,
        'due_date': dueDate,
        'status': status.name,
        'description': description,
        'paid_at': paidAt,
        'created_at': createdAt,
        'updated_at': updatedAt,
      };
}
