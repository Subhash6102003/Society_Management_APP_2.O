import 'package:supabase_flutter/supabase_flutter.dart';
import '../models/user_model.dart';
import '../core/constants/app_constants.dart';

/// Handles all Supabase auth operations — sign up, sign in, sign out.
/// The raw auth result is kept separate from the app's UserModel to avoid
/// tight coupling between auth state and profile data.
class AuthService {
  AuthService._();
  static final AuthService instance = AuthService._();

  SupabaseClient get _client => Supabase.instance.client;

  // ── Current session ──────────────────────────────────────────────────────────

  User? get currentAuthUser => _client.auth.currentUser;
  bool get isLoggedIn => currentAuthUser != null;
  Stream<AuthState> get authStateStream => _client.auth.onAuthStateChange;

  // ── Sign Up ──────────────────────────────────────────────────────────────────

  /// Creates a Supabase auth account and inserts a minimal user row.
  /// Full profile is completed in a second step (CreateProfileScreen).
  Future<UserModel> signUp({
    required String email,
    required String password,
    required UserRole role,
    required String name,
  }) async {
    final response = await _client.auth.signUp(
      email: email,
      password: password,
      data: {
        'name': name,
        'role': role.name,
      },
    );

    final authUser = response.user;
    if (authUser == null) {
      throw Exception('Sign up failed — no user returned.');
    }

    // If Supabase requires email confirmation, there is no active session here.
    // The DB trigger already created the row via SECURITY DEFINER, so we only
    // attempt the upsert if we have an active session.
    final hasSession = response.session != null;
    final now = DateTime.now().millisecondsSinceEpoch;

    if (hasSession) {
      try {
        await _client.from(AppConstants.tableUsers).upsert(
          {
            'id': authUser.id,
            'email': email,
            'name': name,
            'role': role.name,
            'approval_status': 'pending',
            'is_profile_complete': false,
            'is_onboarded': false,
            'is_blocked': false,
            'phone_number': '',
            'profile_photo_url': '',
            'id_proof_url': '',
            'flat_number': '',
            'tower_block': '',
            'house_number': '',
            'tenant_of': null,
            'created_at': now,
            'updated_at': now,
          },
          onConflict: 'id',
        );
      } catch (_) {
        // Fallback: trigger already created the row, just update name/role
        try {
          await _client.from(AppConstants.tableUsers).update({
            'name': name,
            'role': role.name,
            'updated_at': now,
          }).eq('id', authUser.id);
        } catch (_) {
          // Trigger row is sufficient — profile step will fix the rest
        }
      }
    }
    // If no session (email confirmation required), the trigger already inserted
    // the row with name+role from metadata. Profile completion will update it.

    return UserModel(
      id: authUser.id,
      email: email,
      name: name,
      role: role,
      approvalStatus: ApprovalStatus.pending,
      isProfileComplete: false,
      createdAt: now,
      updatedAt: now,
    );
  }

  // ── Sign In ──────────────────────────────────────────────────────────────────

  Future<UserModel> signIn({
    required String email,
    required String password,
  }) async {
    final response = await _client.auth.signInWithPassword(
      email: email,
      password: password,
    );

    final authUser = response.user;
    if (authUser == null) throw Exception('Login failed.');

    // Fetch full user profile from database
    final data = await _client
        .from(AppConstants.tableUsers)
        .select()
        .eq('id', authUser.id)
        .single();

    final userModel = UserModel.fromJson(data);

    // Gate: blocked users are rejected immediately
    if (userModel.isBlocked) {
      await signOut();
      throw Exception('Your account has been blocked. Contact admin.');
    }

    return userModel;
  }

  // ── Sign Out ─────────────────────────────────────────────────────────────────

  Future<void> signOut() async {
    await _client.auth.signOut();
  }

  // ── Password Reset ───────────────────────────────────────────────────────────

  Future<void> sendPasswordResetEmail(String email) async {
    await _client.auth.resetPasswordForEmail(email);
  }

  // ── Get current user profile ─────────────────────────────────────────────────

  Future<UserModel?> getCurrentUserProfile() async {
    final authUser = currentAuthUser;
    if (authUser == null) return null;

    final data = await _client
        .from(AppConstants.tableUsers)
        .select()
        .eq('id', authUser.id)
        .maybeSingle();

    if (data == null) return null;
    return UserModel.fromJson(data);
  }

  // ── Update profile ───────────────────────────────────────────────────────────

  Future<UserModel> completeProfile({
    required String userId,
    required String name,
    required String phoneNumber,
    String flatNumber = '',
    String towerBlock = '',
    String profilePhotoUrl = '',
    String idProofUrl = '',
    required UserRole role,
  }) async {
    // Use the SECURITY DEFINER RPC to bypass any RLS edge-cases.
    // Falls back to direct UPDATE if the RPC doesn't exist yet.
    try {
      final rows = await _client.rpc('complete_user_profile', params: {
        'p_user_id': userId,
        'p_name': name,
        'p_phone_number': phoneNumber,
        'p_flat_number': flatNumber,
        'p_tower_block': towerBlock,
        'p_profile_photo_url': profilePhotoUrl,
        'p_id_proof_url': idProofUrl,
      });

      final List<dynamic> list = rows as List<dynamic>;
      if (list.isEmpty) {
        throw Exception('Profile update failed — no rows returned. '
            'Please run Section 7 of supabase_debug_and_fix.sql in your Supabase SQL Editor.');
      }
      return UserModel.fromJson(list.first as Map<String, dynamic>);
    } catch (e) {
      // RPC might not exist yet — fall back to direct UPDATE
      if (e.toString().contains('complete_user_profile') ||
          e.toString().contains('function') ||
          e.toString().contains('42883')) {
        return _completeProfileDirect(
          userId: userId,
          name: name,
          phoneNumber: phoneNumber,
          flatNumber: flatNumber,
          towerBlock: towerBlock,
          profilePhotoUrl: profilePhotoUrl,
          idProofUrl: idProofUrl,
        );
      }
      rethrow;
    }
  }

  Future<UserModel> _completeProfileDirect({
    required String userId,
    required String name,
    required String phoneNumber,
    String flatNumber = '',
    String towerBlock = '',
    String profilePhotoUrl = '',
    String idProofUrl = '',
  }) async {
    final now = DateTime.now().millisecondsSinceEpoch;

    final data = await _client
        .from(AppConstants.tableUsers)
        .update({
          'name': name,
          'phone_number': phoneNumber,
          'flat_number': flatNumber,
          'tower_block': towerBlock,
          'profile_photo_url': profilePhotoUrl,
          'id_proof_url': idProofUrl,
          'is_profile_complete': true,
          'is_onboarded': true,
          'approval_status': 'pending',
          'updated_at': now,
        })
        .eq('id', userId)
        .select()
        .single();

    return UserModel.fromJson(data);
  }
}
