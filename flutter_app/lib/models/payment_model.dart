// ─── Payment Status ───────────────────────────────────────────────────────────
// 'success' = resident marked as paid in the app (manual tracking)
// 'pending' = bill exists but not yet marked paid
// 'overdue' = past due date and still unpaid

enum PaymentStatus {
  pending,
  success,
  overdue;

  String get displayName {
    switch (this) {
      case PaymentStatus.pending:
        return 'Pending';
      case PaymentStatus.success:
        return 'Paid';
      case PaymentStatus.overdue:
        return 'Overdue';
    }
  }

  static PaymentStatus fromString(String value) {
    return PaymentStatus.values.firstWhere(
      (s) => s.name.toUpperCase() == value.toUpperCase(),
      orElse: () => PaymentStatus.pending,
    );
  }

  bool get isPaid => this == PaymentStatus.success;
}

// ─── Payment Type ─────────────────────────────────────────────────────────────

enum PaymentType {
  maintenance,
  worker,
  maid,
  other;

  static PaymentType fromString(String value) {
    return PaymentType.values.firstWhere(
      (t) => t.name.toUpperCase() == value.toUpperCase(),
      orElse: () => PaymentType.other,
    );
  }
}

// ─── Payment Model ────────────────────────────────────────────────────────────

class PaymentModel {
  final String id;
  final String billId;
  final String userId;
  final String userName;
  final String flatNumber;
  final String towerBlock;
  final double amount;
  final PaymentStatus status;
  final PaymentType type;
  final String note;        // optional note from resident when marking paid
  final int paidAt;         // timestamp when resident marked as paid
  final int createdAt;
  final int updatedAt;

  const PaymentModel({
    required this.id,
    this.billId = '',
    this.userId = '',
    this.userName = '',
    this.flatNumber = '',
    this.towerBlock = '',
    this.amount = 0.0,
    this.status = PaymentStatus.pending,
    this.type = PaymentType.maintenance,
    this.note = '',
    this.paidAt = 0,
    this.createdAt = 0,
    this.updatedAt = 0,
  });

  factory PaymentModel.fromJson(Map<String, dynamic> json) {
    return PaymentModel(
      id: json['id'] as String? ?? '',
      billId: json['bill_id'] as String? ?? '',
      userId: json['user_id'] as String? ?? '',
      userName: json['user_name'] as String? ?? '',
      flatNumber: json['flat_number'] as String? ?? '',
      towerBlock: json['tower_block'] as String? ?? '',
      amount: (json['amount'] as num?)?.toDouble() ?? 0.0,
      status: PaymentStatus.fromString(json['status'] as String? ?? 'PENDING'),
      type: PaymentType.fromString(json['type'] as String? ?? 'MAINTENANCE'),
      note: json['note'] as String? ?? '',
      paidAt: (json['paid_at'] as num?)?.toInt() ?? 0,
      createdAt: (json['created_at'] as num?)?.toInt() ?? 0,
      updatedAt: (json['updated_at'] as num?)?.toInt() ?? 0,
    );
  }

  Map<String, dynamic> toJson() => {
        'id': id,
        'bill_id': billId,
        'user_id': userId,
        'user_name': userName,
        'flat_number': flatNumber,
        'tower_block': towerBlock,
        'amount': amount,
        'status': status.name,
        'type': type.name,
        'note': note,
        'paid_at': paidAt,
        'created_at': createdAt,
        'updated_at': updatedAt,
      };
}
