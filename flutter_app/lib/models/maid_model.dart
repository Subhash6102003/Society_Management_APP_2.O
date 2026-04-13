// ─── Maid Slot Model ──────────────────────────────────────────────────────────

class MaidSlotModel {
  final String id;
  final String maidId;
  final String maidName;
  final String flatId;
  final String flatNumber;
  final String towerBlock;
  final String residentId;
  final String timeSlot; // e.g. "08:00 AM - 09:00 AM"
  final List<String> days; // e.g. ["MON", "TUE", "WED"]
  final bool isActive;
  final int createdAt;
  final int updatedAt;

  const MaidSlotModel({
    required this.id,
    this.maidId = '',
    this.maidName = '',
    this.flatId = '',
    this.flatNumber = '',
    this.towerBlock = '',
    this.residentId = '',
    this.timeSlot = '',
    this.days = const [],
    this.isActive = true,
    this.createdAt = 0,
    this.updatedAt = 0,
  });

  factory MaidSlotModel.fromJson(Map<String, dynamic> json) {
    return MaidSlotModel(
      id: json['id'] as String? ?? '',
      maidId: json['maid_id'] as String? ?? '',
      maidName: json['maid_name'] as String? ?? '',
      flatId: json['flat_id'] as String? ?? '',
      flatNumber: json['flat_number'] as String? ?? '',
      towerBlock: json['tower_block'] as String? ?? '',
      residentId: json['resident_id'] as String? ?? '',
      timeSlot: json['time_slot'] as String? ?? '',
      days: List<String>.from(json['days'] as List? ?? []),
      isActive: json['is_active'] as bool? ?? true,
      createdAt: (json['created_at'] as num?)?.toInt() ?? 0,
      updatedAt: (json['updated_at'] as num?)?.toInt() ?? 0,
    );
  }

  Map<String, dynamic> toJson() => {
        'id': id,
        'maid_id': maidId,
        'maid_name': maidName,
        'flat_id': flatId,
        'flat_number': flatNumber,
        'tower_block': towerBlock,
        'resident_id': residentId,
        'time_slot': timeSlot,
        'days': days,
        'is_active': isActive,
        'created_at': createdAt,
        'updated_at': updatedAt,
      };
}

// ─── Maid Attendance Model ────────────────────────────────────────────────────

class MaidAttendanceModel {
  final String id;
  final String maidId;
  final String maidName;
  final String flatNumber;
  final String towerBlock;
  final bool isDutyOn;
  final int dutyOnTime;
  final int dutyOffTime;
  final int date;
  final int createdAt;

  const MaidAttendanceModel({
    required this.id,
    this.maidId = '',
    this.maidName = '',
    this.flatNumber = '',
    this.towerBlock = '',
    this.isDutyOn = false,
    this.dutyOnTime = 0,
    this.dutyOffTime = 0,
    this.date = 0,
    this.createdAt = 0,
  });

  factory MaidAttendanceModel.fromJson(Map<String, dynamic> json) {
    return MaidAttendanceModel(
      id: json['id'] as String? ?? '',
      maidId: json['maid_id'] as String? ?? '',
      maidName: json['maid_name'] as String? ?? '',
      flatNumber: json['flat_number'] as String? ?? '',
      towerBlock: json['tower_block'] as String? ?? '',
      isDutyOn: json['is_duty_on'] as bool? ?? false,
      dutyOnTime: (json['duty_on_time'] as num?)?.toInt() ?? 0,
      dutyOffTime: (json['duty_off_time'] as num?)?.toInt() ?? 0,
      date: (json['date'] as num?)?.toInt() ?? 0,
      createdAt: (json['created_at'] as num?)?.toInt() ?? 0,
    );
  }

  Map<String, dynamic> toJson() => {
        'id': id,
        'maid_id': maidId,
        'maid_name': maidName,
        'flat_number': flatNumber,
        'tower_block': towerBlock,
        'is_duty_on': isDutyOn,
        'duty_on_time': dutyOnTime,
        'duty_off_time': dutyOffTime,
        'date': date,
        'created_at': createdAt,
      };
}
