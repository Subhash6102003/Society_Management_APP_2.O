// ─── Notice Category ──────────────────────────────────────────────────────────

enum NoticeCategory {
  general,
  maintenance,
  event,
  emergency,
  payment,
  security;

  String get displayName => switch (this) {
    NoticeCategory.general => 'General',
    NoticeCategory.maintenance => 'Maintenance',
    NoticeCategory.event => 'Event',
    NoticeCategory.emergency => 'Emergency',
    NoticeCategory.payment => 'Payment',
    NoticeCategory.security => 'Security',
  };

  static NoticeCategory fromString(String value) => switch (value.toUpperCase()) {
    'MAINTENANCE' => NoticeCategory.maintenance,
    'EVENT' => NoticeCategory.event,
    'EMERGENCY' => NoticeCategory.emergency,
    'PAYMENT' => NoticeCategory.payment,
    'SECURITY' => NoticeCategory.security,
    _ => NoticeCategory.general,
  };
}

// ─── Notice Model ─────────────────────────────────────────────────────────────

class NoticeModel {
  final String id;
  final String title;
  final String body;       // stored as 'content' in Supabase
  final String imageUrl;
  final String createdBy;      // stored as 'author_id' in Supabase
  final String createdByName;  // stored as 'author_name' in Supabase
  final NoticeCategory category;
  final List<String> targetRoles;
  final bool isPinned;
  final int createdAt;
  final int updatedAt;

  const NoticeModel({
    required this.id,
    this.title = '',
    this.body = '',
    this.imageUrl = '',
    this.createdBy = '',
    this.createdByName = '',
    this.category = NoticeCategory.general,
    this.targetRoles = const [],
    this.isPinned = false,
    this.createdAt = 0,
    this.updatedAt = 0,
  });

  factory NoticeModel.fromJson(Map<String, dynamic> json) {
    return NoticeModel(
      id: json['id'] as String? ?? '',
      title: json['title'] as String? ?? '',
      body: json['content'] as String? ?? '',          // column: content → field: body
      imageUrl: json['image_url'] as String? ?? '',
      createdBy: json['author_id'] as String? ?? '',   // column: author_id → field: createdBy
      createdByName: json['author_name'] as String? ?? '',
      category: NoticeCategory.fromString(json['category'] as String? ?? 'general'),
      targetRoles: List<String>.from(json['target_roles'] as List? ?? []),
      isPinned: json['is_pinned'] as bool? ?? false,
      createdAt: (json['created_at'] as num?)?.toInt() ?? 0,
      updatedAt: (json['updated_at'] as num?)?.toInt() ?? 0,
    );
  }

  Map<String, dynamic> toJson() => {
        'id': id,
        'title': title,
        'content': body,
        'image_url': imageUrl,
        'author_id': createdBy,
        'author_name': createdByName,
        'category': category.name,
        'target_roles': targetRoles,
        'is_pinned': isPinned,
        'created_at': createdAt,
        'updated_at': updatedAt,
      };
}
