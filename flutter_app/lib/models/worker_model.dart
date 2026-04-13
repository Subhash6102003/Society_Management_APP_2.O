// ─── Worker Category ─────────────────────────────────────────────────────────

enum WorkerCategory {
  plumber,
  electrician,
  carpenter,
  cleaner,
  gardener,
  painter,
  security,
  other;

  String get displayName => switch (this) {
    WorkerCategory.plumber => 'Plumber',
    WorkerCategory.electrician => 'Electrician',
    WorkerCategory.carpenter => 'Carpenter',
    WorkerCategory.cleaner => 'Cleaner',
    WorkerCategory.gardener => 'Gardener',
    WorkerCategory.painter => 'Painter',
    WorkerCategory.security => 'Security',
    WorkerCategory.other => 'Other',
  };

  static WorkerCategory fromString(String value) => switch (value.toUpperCase()) {
    'PLUMBER' => WorkerCategory.plumber,
    'ELECTRICIAN' => WorkerCategory.electrician,
    'CARPENTER' => WorkerCategory.carpenter,
    'CLEANER' => WorkerCategory.cleaner,
    'GARDENER' => WorkerCategory.gardener,
    'PAINTER' => WorkerCategory.painter,
    'SECURITY' => WorkerCategory.security,
    _ => WorkerCategory.other,
  };
}

// ─── Work Order Status ────────────────────────────────────────────────────────

enum WorkOrderStatus {
  pending,
  accepted,
  rejected,
  inProgress,
  completed,
  cancelled;

  String get displayName => switch (this) {
    WorkOrderStatus.pending => 'Pending',
    WorkOrderStatus.accepted => 'Accepted',
    WorkOrderStatus.rejected => 'Rejected',
    WorkOrderStatus.inProgress => 'In Progress',
    WorkOrderStatus.completed => 'Completed',
    WorkOrderStatus.cancelled => 'Cancelled',
  };

  static WorkOrderStatus fromString(String value) => switch (value.toUpperCase()) {
    'ACCEPTED' => WorkOrderStatus.accepted,
    'REJECTED' => WorkOrderStatus.rejected,
    'IN_PROGRESS' || 'INPROGRESS' => WorkOrderStatus.inProgress,
    'COMPLETED' => WorkOrderStatus.completed,
    'CANCELLED' => WorkOrderStatus.cancelled,
    _ => WorkOrderStatus.pending,
  };
}

// ─── Worker Model ─────────────────────────────────────────────────────────────

class WorkerModel {
  final String id;
  final String userId;
  final String name;
  final String phoneNumber;
  final String profilePhotoUrl;
  final String idProofUrl;
  final List<String> skills;
  final WorkerCategory category;
  final bool isAvailable;
  final bool isDutyOn;
  final double rating;
  final int totalRatings;
  final int totalJobs;
  final double totalEarnings;
  final List<String> assignedFlats;
  final int createdAt;
  final int updatedAt;

  const WorkerModel({
    required this.id,
    required this.userId,
    this.name = '',
    this.phoneNumber = '',
    this.profilePhotoUrl = '',
    this.idProofUrl = '',
    this.skills = const [],
    this.category = WorkerCategory.other,
    this.isAvailable = true,
    this.isDutyOn = false,
    this.rating = 0.0,
    this.totalRatings = 0,
    this.totalJobs = 0,
    this.totalEarnings = 0.0,
    this.assignedFlats = const [],
    this.createdAt = 0,
    this.updatedAt = 0,
  });

  factory WorkerModel.fromJson(Map<String, dynamic> json) {
    return WorkerModel(
      id: json['id'] as String? ?? '',
      userId: json['user_id'] as String? ?? '',
      name: json['name'] as String? ?? '',
      phoneNumber: json['phone_number'] as String? ?? '',
      profilePhotoUrl: json['profile_photo_url'] as String? ?? '',
      idProofUrl: json['id_proof_url'] as String? ?? '',
      skills: List<String>.from(json['skills'] as List? ?? []),
      category: WorkerCategory.fromString(json['category'] as String? ?? 'OTHER'),
      isAvailable: json['is_available'] as bool? ?? true,
      isDutyOn: json['is_duty_on'] as bool? ?? false,
      rating: (json['rating'] as num?)?.toDouble() ?? 0.0,
      totalRatings: (json['total_ratings'] as num?)?.toInt() ?? 0,
      totalJobs: (json['total_jobs'] as num?)?.toInt() ?? 0,
      totalEarnings: (json['total_earnings'] as num?)?.toDouble() ?? 0.0,
      assignedFlats: List<String>.from(json['assigned_flats'] as List? ?? []),
      createdAt: (json['created_at'] as num?)?.toInt() ?? 0,
      updatedAt: (json['updated_at'] as num?)?.toInt() ?? 0,
    );
  }

  Map<String, dynamic> toJson() => {
        'id': id,
        'user_id': userId,
        'name': name,
        'phone_number': phoneNumber,
        'profile_photo_url': profilePhotoUrl,
        'id_proof_url': idProofUrl,
        'skills': skills,
        'category': category.name,
        'is_available': isAvailable,
        'is_duty_on': isDutyOn,
        'rating': rating,
        'total_ratings': totalRatings,
        'total_jobs': totalJobs,
        'total_earnings': totalEarnings,
        'assigned_flats': assignedFlats,
        'created_at': createdAt,
        'updated_at': updatedAt,
      };
}

// ─── Work Order ───────────────────────────────────────────────────────────────

class WorkOrderModel {
  final String id;
  final String workerId;
  final String workerName;
  final String flatId;
  final String flatNumber;
  final String towerBlock;
  final String residentId;
  final String residentName;
  final String title;
  final String description;
  final WorkerCategory category;
  final WorkOrderStatus status;
  final double amount;
  final bool isPaid;
  final String paymentId;
  final double rating;
  final String feedback;
  final int scheduledAt;
  final int startedAt;
  final int completedAt;
  final int createdAt;
  final int updatedAt;

  const WorkOrderModel({
    required this.id,
    this.workerId = '',
    this.workerName = '',
    this.flatId = '',
    this.flatNumber = '',
    this.towerBlock = '',
    this.residentId = '',
    this.residentName = '',
    this.title = '',
    this.description = '',
    this.category = WorkerCategory.other,
    this.status = WorkOrderStatus.pending,
    this.amount = 0.0,
    this.isPaid = false,
    this.paymentId = '',
    this.rating = 0.0,
    this.feedback = '',
    this.scheduledAt = 0,
    this.startedAt = 0,
    this.completedAt = 0,
    this.createdAt = 0,
    this.updatedAt = 0,
  });

  factory WorkOrderModel.fromJson(Map<String, dynamic> json) {
    return WorkOrderModel(
      id: json['id'] as String? ?? '',
      workerId: json['worker_id'] as String? ?? '',
      workerName: json['worker_name'] as String? ?? '',
      flatId: json['flat_id'] as String? ?? '',
      flatNumber: json['flat_number'] as String? ?? '',
      towerBlock: json['tower_block'] as String? ?? '',
      residentId: json['resident_id'] as String? ?? '',
      residentName: json['resident_name'] as String? ?? '',
      title: json['title'] as String? ?? '',
      description: json['description'] as String? ?? '',
      category: WorkerCategory.fromString(json['category'] as String? ?? 'other'),
      status: WorkOrderStatus.fromString(json['status'] as String? ?? 'pending'),
      amount: (json['amount'] as num?)?.toDouble() ?? 0.0,
      isPaid: json['is_paid'] as bool? ?? false,
      paymentId: json['payment_id'] as String? ?? '',
      rating: (json['rating'] as num?)?.toDouble() ?? 0.0,
      feedback: json['feedback'] as String? ?? '',
      scheduledAt: (json['scheduled_at'] as num?)?.toInt() ?? 0,
      startedAt: (json['started_at'] as num?)?.toInt() ?? 0,
      completedAt: (json['completed_at'] as num?)?.toInt() ?? 0,
      createdAt: (json['created_at'] as num?)?.toInt() ?? 0,
      updatedAt: (json['updated_at'] as num?)?.toInt() ?? 0,
    );
  }

  Map<String, dynamic> toJson() => {
        'id': id,
        'worker_id': workerId,
        'worker_name': workerName,
        'flat_id': flatId,
        'flat_number': flatNumber,
        'tower_block': towerBlock,
        'resident_id': residentId,
        'resident_name': residentName,
        'title': title,
        'description': description,
        'category': category.name,
        'status': status.name,
        'amount': amount,
        'is_paid': isPaid,
        'payment_id': paymentId,
        'rating': rating,
        'feedback': feedback,
        'scheduled_at': scheduledAt,
        'started_at': startedAt,
        'completed_at': completedAt,
        'created_at': createdAt,
        'updated_at': updatedAt,
      };

  WorkOrderModel copyWith({WorkOrderStatus? status, bool? isPaid, String? paymentId}) {
    return WorkOrderModel(
      id: id,
      workerId: workerId,
      workerName: workerName,
      flatId: flatId,
      flatNumber: flatNumber,
      towerBlock: towerBlock,
      residentId: residentId,
      residentName: residentName,
      title: title,
      description: description,
      category: category,
      status: status ?? this.status,
      amount: amount,
      isPaid: isPaid ?? this.isPaid,
      paymentId: paymentId ?? this.paymentId,
      rating: rating,
      feedback: feedback,
      scheduledAt: scheduledAt,
      startedAt: startedAt,
      completedAt: completedAt,
      createdAt: createdAt,
      updatedAt: updatedAt,
    );
  }
}
