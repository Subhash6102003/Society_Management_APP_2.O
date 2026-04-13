import 'package:flutter/material.dart';
import '../../core/constants/app_colors.dart';

// ─── User Role ────────────────────────────────────────────────────────────────

enum UserRole {
  admin,
  resident,
  tenant,
  securityGuard,
  worker,
  maid;

  String get displayName => switch (this) {
    UserRole.admin => 'Admin',
    UserRole.resident => 'Resident',
    UserRole.tenant => 'Tenant',
    UserRole.securityGuard => 'Security Guard',
    UserRole.worker => 'Worker',
    UserRole.maid => 'Maid',
  };

  Color get color => switch (this) {
    UserRole.admin => AppColors.admin,
    UserRole.resident => AppColors.resident,
    UserRole.tenant => AppColors.tenant,
    UserRole.securityGuard => AppColors.guard,
    UserRole.worker => AppColors.worker,
    UserRole.maid => AppColors.maid,
  };

  /// Whether this role requires flat number during signup.
  bool get requiresFlat => this == UserRole.resident || this == UserRole.tenant;

  static UserRole fromString(String value) => switch (value.toUpperCase()) {
    'ADMIN' => UserRole.admin,
    'TENANT' => UserRole.tenant,
    // DB stores 'securityGuard' → uppercase is 'SECURITYGUARD' (no underscore)
    'SECURITYGUARD' || 'SECURITY_GUARD' => UserRole.securityGuard,
    'WORKER' => UserRole.worker,
    'MAID' => UserRole.maid,
    _ => UserRole.resident,
  };
}

// ─── Approval Status ──────────────────────────────────────────────────────────

enum ApprovalStatus {
  pending,
  approved,
  rejected;

  String get displayName => switch (this) {
    ApprovalStatus.pending => 'Pending',
    ApprovalStatus.approved => 'Approved',
    ApprovalStatus.rejected => 'Rejected',
  };

  static ApprovalStatus fromString(String value) => switch (value.toUpperCase()) {
    'APPROVED' => ApprovalStatus.approved,
    'REJECTED' => ApprovalStatus.rejected,
    _ => ApprovalStatus.pending,
  };
}

// ─── User Model ───────────────────────────────────────────────────────────────

class UserModel {
  final String id;
  final String email;
  final String name;
  final String phoneNumber;
  final String profilePhotoUrl;
  final String idProofUrl;
  final UserRole role;
  final String flatNumber;
  final String towerBlock;
  final String houseNumber;
  final ApprovalStatus approvalStatus;
  final bool isBlocked;
  final bool isProfileComplete;
  final bool isOnboarded;
  final String tenantOf; // resident user ID if this user is a tenant
  final int createdAt;
  final int updatedAt;

  const UserModel({
    required this.id,
    required this.email,
    this.name = '',
    this.phoneNumber = '',
    this.profilePhotoUrl = '',
    this.idProofUrl = '',
    this.role = UserRole.resident,
    this.flatNumber = '',
    this.towerBlock = '',
    this.houseNumber = '',
    this.approvalStatus = ApprovalStatus.pending,
    this.isBlocked = false,
    this.isProfileComplete = false,
    this.isOnboarded = false,
    this.tenantOf = '',
    this.createdAt = 0,
    this.updatedAt = 0,
  });

  factory UserModel.fromJson(Map<String, dynamic> json) {
    return UserModel(
      id: json['id'] as String? ?? '',
      email: json['email'] as String? ?? '',
      name: json['name'] as String? ?? '',
      phoneNumber: json['phone_number'] as String? ?? '',
      profilePhotoUrl: json['profile_photo_url'] as String? ?? '',
      idProofUrl: json['id_proof_url'] as String? ?? '',
      role: UserRole.fromString(json['role'] as String? ?? 'resident'),
      flatNumber: json['flat_number'] as String? ?? '',
      towerBlock: json['tower_block'] as String? ?? '',
      houseNumber: json['house_number'] as String? ?? '',
      approvalStatus: ApprovalStatus.fromString(
        json['approval_status'] as String? ?? 'pending',
      ),
      isBlocked: json['is_blocked'] as bool? ?? false,
      isProfileComplete: json['is_profile_complete'] as bool? ?? false,
      isOnboarded: json['is_onboarded'] as bool? ?? false,
      tenantOf: json['tenant_of'] as String? ?? '',
      createdAt: (json['created_at'] as num?)?.toInt() ?? 0,
      updatedAt: (json['updated_at'] as num?)?.toInt() ?? 0,
    );
  }

  Map<String, dynamic> toJson() => {
        'id': id,
        'email': email,
        'name': name,
        'phone_number': phoneNumber,
        'profile_photo_url': profilePhotoUrl,
        'id_proof_url': idProofUrl,
        'role': role.name,
        'flat_number': flatNumber,
        'tower_block': towerBlock,
        'house_number': houseNumber,
        'approval_status': approvalStatus.name,
        'is_blocked': isBlocked,
        'is_profile_complete': isProfileComplete,
        'is_onboarded': isOnboarded,
        // Send null (not empty string) for UUID foreign key field
        'tenant_of': tenantOf.isEmpty ? null : tenantOf,
        'created_at': createdAt,
        'updated_at': updatedAt,
      };

  UserModel copyWith({
    String? id,
    String? email,
    String? name,
    String? phoneNumber,
    String? profilePhotoUrl,
    String? idProofUrl,
    UserRole? role,
    String? flatNumber,
    String? towerBlock,
    String? houseNumber,
    ApprovalStatus? approvalStatus,
    bool? isBlocked,
    bool? isProfileComplete,
    bool? isOnboarded,
    String? tenantOf,
    int? createdAt,
    int? updatedAt,
  }) {
    return UserModel(
      id: id ?? this.id,
      email: email ?? this.email,
      name: name ?? this.name,
      phoneNumber: phoneNumber ?? this.phoneNumber,
      profilePhotoUrl: profilePhotoUrl ?? this.profilePhotoUrl,
      idProofUrl: idProofUrl ?? this.idProofUrl,
      role: role ?? this.role,
      flatNumber: flatNumber ?? this.flatNumber,
      towerBlock: towerBlock ?? this.towerBlock,
      houseNumber: houseNumber ?? this.houseNumber,
      approvalStatus: approvalStatus ?? this.approvalStatus,
      isBlocked: isBlocked ?? this.isBlocked,
      isProfileComplete: isProfileComplete ?? this.isProfileComplete,
      isOnboarded: isOnboarded ?? this.isOnboarded,
      tenantOf: tenantOf ?? this.tenantOf,
      createdAt: createdAt ?? this.createdAt,
      updatedAt: updatedAt ?? this.updatedAt,
    );
  }

  /// Convenience getters for backwards compatibility with screen code
  String get photoUrl => profilePhotoUrl;
  String? get tower => towerBlock.isEmpty ? null : towerBlock;

  @override
  String toString() => 'UserModel(id: $id, name: $name, role: $role)';
}
