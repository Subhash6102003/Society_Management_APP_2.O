// ─── Complaint Category ───────────────────────────────────────────────────────

enum ComplaintCategory {
  plumbing,
  electrical,
  cleanliness,
  security,
  noise,
  parking,
  maintenance,
  other;

  String get displayName => switch (this) {
    ComplaintCategory.plumbing => 'Plumbing',
    ComplaintCategory.electrical => 'Electrical',
    ComplaintCategory.cleanliness => 'Cleanliness',
    ComplaintCategory.security => 'Security',
    ComplaintCategory.noise => 'Noise',
    ComplaintCategory.parking => 'Parking',
    ComplaintCategory.maintenance => 'Maintenance',
    ComplaintCategory.other => 'Other',
  };

  static ComplaintCategory fromString(String value) => switch (value.toUpperCase()) {
    'PLUMBING' => ComplaintCategory.plumbing,
    'ELECTRICAL' => ComplaintCategory.electrical,
    'CLEANLINESS' => ComplaintCategory.cleanliness,
    'SECURITY' => ComplaintCategory.security,
    'NOISE' => ComplaintCategory.noise,
    'PARKING' => ComplaintCategory.parking,
    'MAINTENANCE' => ComplaintCategory.maintenance,
    _ => ComplaintCategory.other,
  };
}

// ─── Complaint Status ─────────────────────────────────────────────────────────

enum ComplaintStatus {
  open,
  inProgress,
  resolved,
  closed;

  String get displayName => switch (this) {
    ComplaintStatus.open => 'Open',
    ComplaintStatus.inProgress => 'In Progress',
    ComplaintStatus.resolved => 'Resolved',
    ComplaintStatus.closed => 'Closed',
  };

  static ComplaintStatus fromString(String value) => switch (value.toUpperCase()) {
    'IN_PROGRESS' || 'INPROGRESS' => ComplaintStatus.inProgress,
    'RESOLVED' => ComplaintStatus.resolved,
    'CLOSED' => ComplaintStatus.closed,
    _ => ComplaintStatus.open,
  };
}

// ─── Complaint Model ──────────────────────────────────────────────────────────

class ComplaintModel {
  final String id;
  final String userId;
  final String userName;
  final String flatNumber;
  final String towerBlock;
  final String title;
  final String description;
  final ComplaintCategory category;
  final ComplaintStatus status;
  final List<String> imageUrls;
  final String resolution; // admin response
  final int createdAt;
  final int updatedAt;

  // Convenience aliases
  String get residentName => userName;
  String get residentId => userId;

  const ComplaintModel({
    required this.id,
    this.userId = '',
    this.userName = '',
    this.flatNumber = '',
    this.towerBlock = '',
    this.title = '',
    this.description = '',
    this.category = ComplaintCategory.other,
    this.status = ComplaintStatus.open,
    this.imageUrls = const [],
    this.resolution = '',
    this.createdAt = 0,
    this.updatedAt = 0,
  });

  factory ComplaintModel.fromJson(Map<String, dynamic> json) {
    // 'image_url' in Supabase is a single string; domain uses list
    final imageUrl = json['image_url'] as String? ?? '';
    return ComplaintModel(
      id: json['id'] as String? ?? '',
      userId: json['user_id'] as String? ?? '',
      userName: json['user_name'] as String? ?? '',
      flatNumber: json['flat_number'] as String? ?? '',
      towerBlock: json['tower_block'] as String? ?? '',
      title: json['title'] as String? ?? '',
      description: json['description'] as String? ?? '',
      category: ComplaintCategory.fromString(json['category'] as String? ?? 'OTHER'),
      status: ComplaintStatus.fromString(json['status'] as String? ?? 'OPEN'),
      imageUrls: imageUrl.isNotEmpty ? [imageUrl] : [],
      resolution: json['admin_response'] as String? ?? '',
      createdAt: (json['created_at'] as num?)?.toInt() ?? 0,
      updatedAt: (json['updated_at'] as num?)?.toInt() ?? 0,
    );
  }

  Map<String, dynamic> toJson() => {
        'id': id,
        'user_id': userId,
        'user_name': userName,
        'flat_number': flatNumber,
        'tower_block': towerBlock,
        'title': title,
        'description': description,
        'category': category.name,
        'status': status.name,
        'image_url': imageUrls.isNotEmpty ? imageUrls.first : '',
        'admin_response': resolution,
        'created_at': createdAt,
        'updated_at': updatedAt,
      };
}
