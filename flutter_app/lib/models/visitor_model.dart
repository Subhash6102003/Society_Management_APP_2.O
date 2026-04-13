// ─── Visitor Status ───────────────────────────────────────────────────────────

enum VisitorStatus {
  pending,
  approved,
  rejected,
  checkedIn,
  checkedOut,
  expired;

  String get displayName => switch (this) {
    VisitorStatus.pending => 'Pending',
    VisitorStatus.approved => 'Approved',
    VisitorStatus.rejected => 'Rejected',
    VisitorStatus.checkedIn => 'Checked In',
    VisitorStatus.checkedOut => 'Checked Out',
    VisitorStatus.expired => 'Expired',
  };

  static VisitorStatus fromString(String value) => switch (value.toUpperCase()) {
    'APPROVED' => VisitorStatus.approved,
    'REJECTED' => VisitorStatus.rejected,
    'CHECKED_IN' || 'CHECKEDIN' => VisitorStatus.checkedIn,
    'CHECKED_OUT' || 'CHECKEDOUT' => VisitorStatus.checkedOut,
    'EXPIRED' => VisitorStatus.expired,
    _ => VisitorStatus.pending,
  };
}

// ─── Visitor Model ────────────────────────────────────────────────────────────

class VisitorModel {
  final String id;
  final String residentId;
  final String residentName;
  final String flatNumber;
  final String towerBlock;
  final String name;         // visitor_name in Supabase
  final String phoneNumber;  // visitor_phone in Supabase
  final String purpose;
  final String vehicleNumber;
  final String photoUrl;
  final String idProofUrl;
  final VisitorStatus status;
  final int entryTime;
  final int exitTime;
  final int createdAt;
  final int updatedAt;

  /// Convenience getter: returns createdAt as the visit date.
  int get visitDate => createdAt > 0 ? createdAt : entryTime;

  const VisitorModel({
    required this.id,
    this.residentId = '',
    this.residentName = '',
    this.flatNumber = '',
    this.towerBlock = '',
    this.name = '',
    this.phoneNumber = '',
    this.purpose = '',
    this.vehicleNumber = '',
    this.photoUrl = '',
    this.idProofUrl = '',
    this.status = VisitorStatus.pending,
    this.entryTime = 0,
    this.exitTime = 0,
    this.createdAt = 0,
    this.updatedAt = 0,
  });

  factory VisitorModel.fromJson(Map<String, dynamic> json) {
    return VisitorModel(
      id: json['id'] as String? ?? '',
      residentId: json['resident_id'] as String? ?? '',
      residentName: json['resident_name'] as String? ?? '',
      flatNumber: json['flat_number'] as String? ?? '',
      towerBlock: json['tower_block'] as String? ?? '',
      name: json['visitor_name'] as String? ?? '',
      phoneNumber: json['visitor_phone'] as String? ?? '',
      purpose: json['purpose'] as String? ?? '',
      vehicleNumber: json['vehicle_number'] as String? ?? '',
      photoUrl: json['photo_url'] as String? ?? '',
      idProofUrl: json['id_proof_url'] as String? ?? '',
      status: VisitorStatus.fromString(json['status'] as String? ?? 'pending'),
      entryTime: (json['entry_time'] as num?)?.toInt() ?? 0,
      exitTime: (json['exit_time'] as num?)?.toInt() ?? 0,
      createdAt: (json['created_at'] as num?)?.toInt() ?? 0,
      updatedAt: (json['updated_at'] as num?)?.toInt() ?? 0,
    );
  }

  Map<String, dynamic> toJson() => {
        'id': id,
        'resident_id': residentId,
        'resident_name': residentName,
        'flat_number': flatNumber,
        'tower_block': towerBlock,
        'visitor_name': name,
        'visitor_phone': phoneNumber,
        'purpose': purpose,
        'vehicle_number': vehicleNumber,
        'photo_url': photoUrl,
        'id_proof_url': idProofUrl,
        'status': status.name,
        'entry_time': entryTime,
        'exit_time': exitTime,
        'created_at': createdAt,
        'updated_at': updatedAt,
      };
}
