import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:supabase_flutter/supabase_flutter.dart' as sb;
import '../core/demo/demo_mode.dart';
import '../models/user_model.dart';
import '../services/auth_service.dart';

// ─── Auth State ───────────────────────────────────────────────────────────────

/// Enum representing the state of authentication + account status in one place.
enum AuthStatus {
  loading,
  unauthenticated,
  pendingProfile,  // logged in but profile not complete
  pending,         // profile complete, waiting admin approval
  approved,
  rejected,
  blocked,
}

class AuthState {
  final AuthStatus status;
  final UserModel? user;
  final String? errorMessage;

  const AuthState({
    required this.status,
    this.user,
    this.errorMessage,
  });

  AuthState copyWith({
    AuthStatus? status,
    UserModel? user,
    String? errorMessage,
    bool clearError = false,
  }) {
    return AuthState(
      status: status ?? this.status,
      user: user ?? this.user,
      errorMessage: clearError ? null : (errorMessage ?? this.errorMessage),
    );
  }

  bool get isApproved => status == AuthStatus.approved;
  bool get isLoading => status == AuthStatus.loading;
}

// ─── Auth Notifier ────────────────────────────────────────────────────────────

class AuthNotifier extends StateNotifier<AuthState> {
  AuthNotifier() : super(const AuthState(status: AuthStatus.loading)) {
    _init();
  }

  final _auth = AuthService.instance;

  /// Guard flag — prevents the auth-state listener from overwriting state
  /// while signUp() / signIn() are actively running their own DB operations.
  bool _authOpInProgress = false;

  void _init() {
    if (DemoMode.enabled) {
      // In demo mode there is no Supabase session; start unauthenticated
      state = const AuthState(status: AuthStatus.unauthenticated);
      return;
    }
    // Listen to Supabase auth state changes
    _auth.authStateStream.listen((event) async {
      if (event.event == sb.AuthChangeEvent.signedOut) {
        if (!_authOpInProgress) {
          state = const AuthState(status: AuthStatus.unauthenticated);
        }
      } else if (event.event == sb.AuthChangeEvent.signedIn ||
          event.event == sb.AuthChangeEvent.tokenRefreshed) {
        // Skip refresh if a manual signUp/signIn is managing state
        if (!_authOpInProgress) {
          await _refreshUser();
        }
      }
    });

    _refreshUser();
  }

  Future<void> _refreshUser() async {
    try {
      final user = await _auth.getCurrentUserProfile();
      if (user == null) {
        state = const AuthState(status: AuthStatus.unauthenticated);
        return;
      }
      state = AuthState(status: _statusFromUser(user), user: user);
    } catch (_) {
      state = const AuthState(status: AuthStatus.unauthenticated);
    }
  }

  AuthStatus _statusFromUser(UserModel user) {
    if (user.isBlocked) return AuthStatus.blocked;
    if (!user.isProfileComplete) return AuthStatus.pendingProfile;
    switch (user.approvalStatus) {
      case ApprovalStatus.pending:
        return AuthStatus.pending;
      case ApprovalStatus.approved:
        return AuthStatus.approved;
      case ApprovalStatus.rejected:
        return AuthStatus.rejected;
    }
  }

  // ── Public actions ─────────────────────────────────────────

  Future<void> signIn(String email, String password) async {
    _authOpInProgress = true;
    state = state.copyWith(status: AuthStatus.loading, clearError: true);
    // Demo mode: match by email to demo credentials
    if (DemoMode.enabled) {
      final cred = DemoMode.credentials.where((c) => c.email == email.trim()).toList();
      if (cred.isNotEmpty && password == cred.first.password) {
        final user = DemoMode.userForRole(cred.first.role);
        state = AuthState(status: AuthStatus.approved, user: user);
      } else {
        state = const AuthState(
          status: AuthStatus.unauthenticated,
          errorMessage: 'Invalid demo credentials. Use any demo account below.',
        );
      }
      _authOpInProgress = false;
      return;
    }
    try {
      final user = await _auth.signIn(email: email, password: password);
      state = AuthState(status: _statusFromUser(user), user: user);
    } catch (e) {
      state = AuthState(
        status: AuthStatus.unauthenticated,
        errorMessage: e.toString().replaceAll('Exception: ', ''),
      );
    } finally {
      _authOpInProgress = false;
    }
  }

  /// Instantly log in as a demo user for the given role (no network call).
  void demoSignIn(UserRole role) {
    final user = DemoMode.userForRole(role);
    state = AuthState(status: AuthStatus.approved, user: user);
  }

  Future<void> signUp({
    required String email,
    required String password,
    required UserRole role,
    required String name,
  }) async {
    _authOpInProgress = true;
    state = state.copyWith(status: AuthStatus.loading, clearError: true);
    try {
      final user = await _auth.signUp(
        email: email,
        password: password,
        role: role,
        name: name,
      );
      state = AuthState(status: AuthStatus.pendingProfile, user: user);
    } catch (e) {
      state = AuthState(
        status: AuthStatus.unauthenticated,
        errorMessage: e.toString().replaceAll('Exception: ', ''),
      );
    } finally {
      _authOpInProgress = false;
    }
  }

  Future<void> completeProfile({
    required String name,
    required String phoneNumber,
    String flatNumber = '',
    String towerBlock = '',
    String profilePhotoUrl = '',
    String idProofUrl = '',
  }) async {
    final user = state.user;
    if (user == null) return;

    state = state.copyWith(status: AuthStatus.loading, clearError: true);
    try {
      final updated = await _auth.completeProfile(
        userId: user.id,
        name: name,
        phoneNumber: phoneNumber,
        flatNumber: flatNumber,
        towerBlock: towerBlock,
        profilePhotoUrl: profilePhotoUrl,
        idProofUrl: idProofUrl,
        role: user.role,
      );
      state = AuthState(status: _statusFromUser(updated), user: updated);
    } catch (e) {
      state = state.copyWith(
        status: AuthStatus.pendingProfile,
        errorMessage: e.toString().replaceAll('Exception: ', ''),
      );
    }
  }

  Future<void> signOut() async {
    if (!DemoMode.enabled) await _auth.signOut();
    state = const AuthState(status: AuthStatus.unauthenticated);
  }

  Future<void> sendPasswordReset(String email) async {
    await _auth.sendPasswordResetEmail(email);
  }

  /// Force-refresh user from DB (e.g. after admin action).
  Future<void> refresh() => _refreshUser();
}

// ─── Provider ─────────────────────────────────────────────────────────────────

final authProvider = StateNotifierProvider<AuthNotifier, AuthState>(
  (_) => AuthNotifier(),
);

/// Convenience provider — just the current UserModel (nullable).
final currentUserProvider = Provider<UserModel?>((ref) {
  return ref.watch(authProvider).user;
});
