import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import '../../../core/constants/app_colors.dart';
import '../../../core/utils/validators.dart';
import '../../../models/user_model.dart';
import '../../../providers/auth_provider.dart';
import '../../../routes/app_router.dart';
import '../../../widgets/common_widgets.dart';

/// Sign-up screen — collect email + password.
/// Role is passed as [GoRouterState.extra] from SelectRoleScreen.
class SignupScreen extends ConsumerStatefulWidget {
  const SignupScreen({super.key});

  @override
  ConsumerState<SignupScreen> createState() => _SignupScreenState();
}

class _SignupScreenState extends ConsumerState<SignupScreen> {
  final _formKey = GlobalKey<FormState>();
  final _nameCtrl = TextEditingController();
  final _emailCtrl = TextEditingController();
  final _passwordCtrl = TextEditingController();
  final _confirmCtrl = TextEditingController();
  bool _obscurePass = true;
  bool _obscureConfirm = true;
  bool _isLoading = false;

  UserRole _role = UserRole.resident;

  @override
  void didChangeDependencies() {
    super.didChangeDependencies();
    // Role passed from SelectRoleScreen via GoRouter extra
    final extra = GoRouterState.of(context).extra;
    if (extra is UserRole) _role = extra;
  }

  @override
  void dispose() {
    _nameCtrl.dispose();
    _emailCtrl.dispose();
    _passwordCtrl.dispose();
    _confirmCtrl.dispose();
    super.dispose();
  }

  Future<void> _signUp() async {
    if (!_formKey.currentState!.validate()) return;

    setState(() => _isLoading = true);

    await ref.read(authProvider.notifier).signUp(
          email: _emailCtrl.text.trim(),
          password: _passwordCtrl.text,
          role: _role,
          name: _nameCtrl.text.trim(),
        );

    if (mounted) setState(() => _isLoading = false);

    final authState = ref.read(authProvider);
    if (authState.errorMessage != null && mounted) {
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(
          content: Text(authState.errorMessage!),
          backgroundColor: AppColors.error,
        ),
      );
    }
    // On success → router redirects to CreateProfileScreen
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: const Color(0xFFF5F5F5),
      appBar: AppBar(
        title: const Text('Create Account'),
        leading: BackButton(onPressed: () => context.pop()),
      ),
      body: SingleChildScrollView(
        padding: const EdgeInsets.all(24),
        child: Form(
          key: _formKey,
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              // ── Role badge ───────────────────────────────────────────────────
              Row(
                children: [
                  StatusBadge(label: _role.displayName, color: _role.color),
                  const SizedBox(width: 8),
                  TextButton(
                    onPressed: () => context.pop(),
                    child: const Text('Change Role'),
                  ),
                ],
              ),

              const SizedBox(height: 24),

              // ── Name ─────────────────────────────────────────────────────────
              AppTextField(
                controller: _nameCtrl,
                hint: 'Full Name',
                prefixIcon: Icons.badge_outlined,
                validator: (v) => AppValidators.required(v, label: 'Name'),
              ),

              const SizedBox(height: 16),

              // ── Email ─────────────────────────────────────────────────────────
              AppTextField(
                controller: _emailCtrl,
                hint: 'Email Address',
                prefixIcon: Icons.email_outlined,
                keyboardType: TextInputType.emailAddress,
                validator: AppValidators.email,
              ),

              const SizedBox(height: 16),

              // ── Password ──────────────────────────────────────────────────────
              AppTextField(
                controller: _passwordCtrl,
                hint: 'Password',
                prefixIcon: Icons.lock_outline,
                obscureText: _obscurePass,
                validator: AppValidators.password,
                suffix: IconButton(
                  icon: Icon(
                    _obscurePass
                        ? Icons.visibility_outlined
                        : Icons.visibility_off_outlined,
                    color: AppColors.textSecondary,
                  ),
                  onPressed: () => setState(() => _obscurePass = !_obscurePass),
                ),
              ),

              const SizedBox(height: 16),

              // ── Confirm password ──────────────────────────────────────────────
              AppTextField(
                controller: _confirmCtrl,
                hint: 'Confirm Password',
                prefixIcon: Icons.lock_outline,
                obscureText: _obscureConfirm,
                validator: (v) =>
                    AppValidators.confirmPassword(v, _passwordCtrl.text),
                suffix: IconButton(
                  icon: Icon(
                    _obscureConfirm
                        ? Icons.visibility_outlined
                        : Icons.visibility_off_outlined,
                    color: AppColors.textSecondary,
                  ),
                  onPressed: () =>
                      setState(() => _obscureConfirm = !_obscureConfirm),
                ),
              ),

              const SizedBox(height: 32),

              // ── Sign Up button ────────────────────────────────────────────────
              PrimaryButton(
                label: 'Create Account',
                onPressed: _signUp,
                isLoading: _isLoading,
              ),

              const SizedBox(height: 20),

              Center(
                child: TextButton(
                  onPressed: () => context.go(AppRoutes.login),
                  child: const Text(
                    'Already have an account? Login',
                    style: TextStyle(color: AppColors.primary),
                  ),
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }
}
