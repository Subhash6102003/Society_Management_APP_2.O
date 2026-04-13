import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../../core/constants/app_colors.dart';
import '../../../providers/auth_provider.dart';

/// Shown while admin has not yet approved the account.
class PendingApprovalScreen extends ConsumerWidget {
  const PendingApprovalScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final user = ref.watch(authProvider).user;

    return Scaffold(
      backgroundColor: const Color(0xFFF8F6F6),
      body: SafeArea(
        child: SingleChildScrollView(
          padding: const EdgeInsets.all(24),
          child: Column(
            children: [
              const SizedBox(height: 32),

              // Icon
              Container(
                width: 100,
                height: 100,
                decoration: BoxDecoration(
                  color: AppColors.warning.withOpacity(0.12),
                  shape: BoxShape.circle,
                ),
                child: const Icon(
                  Icons.hourglass_top_rounded,
                  size: 52,
                  color: AppColors.warning,
                ),
              ),
              const SizedBox(height: 24),

              // Title
              const Text(
                'Pending Approval',
                style: TextStyle(
                  fontSize: 26,
                  fontWeight: FontWeight.bold,
                  color: Color(0xFF212121),
                ),
                textAlign: TextAlign.center,
              ),
              const SizedBox(height: 10),
              Text(
                user != null
                    ? 'Hi ${user.name.split(' ').first}! Your ${user.role.displayName.toLowerCase()} account is under review.'
                    : 'Your account is under review by the society admin.',
                style: const TextStyle(
                  fontSize: 14,
                  color: Color(0xFF757575),
                  height: 1.5,
                ),
                textAlign: TextAlign.center,
              ),
              const SizedBox(height: 40),

              // Step progress card
              Container(
                padding: const EdgeInsets.all(20),
                decoration: BoxDecoration(
                  color: Colors.white,
                  borderRadius: BorderRadius.circular(16),
                  boxShadow: [
                    BoxShadow(
                      color: Colors.black.withOpacity(0.05),
                      blurRadius: 8,
                      offset: const Offset(0, 2),
                    ),
                  ],
                ),
                child: Column(
                  children: [
                    _StepRow(
                      icon: Icons.person_add_alt_1,
                      label: 'Account Created',
                      status: _StepStatus.done,
                    ),
                    _StepDivider(done: true),
                    _StepRow(
                      icon: Icons.assignment_ind,
                      label: 'Profile Submitted',
                      status: user?.isProfileComplete == true
                          ? _StepStatus.done
                          : _StepStatus.active,
                    ),
                    _StepDivider(done: user?.isProfileComplete == true),
                    _StepRow(
                      icon: Icons.admin_panel_settings,
                      label: 'Admin Review',
                      status: user?.isProfileComplete == true
                          ? _StepStatus.active
                          : _StepStatus.waiting,
                    ),
                    _StepDivider(done: false),
                    _StepRow(
                      icon: Icons.verified_user,
                      label: 'Access Granted',
                      status: _StepStatus.waiting,
                    ),
                  ],
                ),
              ),
              const SizedBox(height: 28),

              // Info box
              Container(
                padding: const EdgeInsets.all(16),
                decoration: BoxDecoration(
                  color: AppColors.primary.withOpacity(0.07),
                  borderRadius: BorderRadius.circular(12),
                  border: Border.all(color: AppColors.primary.withOpacity(0.2)),
                ),
                child: Row(
                  children: [
                    Icon(Icons.info_outline, color: AppColors.primary, size: 20),
                    const SizedBox(width: 12),
                    const Expanded(
                      child: Text(
                        'The society admin will verify your identity and approve your account. You will get access once approved.',
                        style: TextStyle(fontSize: 13, color: Color(0xFF424242), height: 1.5),
                      ),
                    ),
                  ],
                ),
              ),
              const SizedBox(height: 32),

              // Refresh button
              SizedBox(
                width: double.infinity,
                child: ElevatedButton.icon(
                  onPressed: () => ref.read(authProvider.notifier).refresh(),
                  icon: const Icon(Icons.refresh),
                  label: const Text('Check Approval Status'),
                  style: ElevatedButton.styleFrom(
                    backgroundColor: AppColors.primary,
                    foregroundColor: Colors.white,
                    padding: const EdgeInsets.symmetric(vertical: 14),
                  ),
                ),
              ),
              const SizedBox(height: 12),
              TextButton.icon(
                onPressed: () => ref.read(authProvider.notifier).signOut(),
                icon: const Icon(Icons.logout, size: 18),
                label: const Text('Logout'),
                style: TextButton.styleFrom(foregroundColor: AppColors.error),
              ),
              const SizedBox(height: 16),
            ],
          ),
        ),
      ),
    );
  }
}

// ─── Step Row ───────────────────────────────────────────────────────────────

enum _StepStatus { done, active, waiting }

class _StepRow extends StatelessWidget {
  final IconData icon;
  final String label;
  final _StepStatus status;

  const _StepRow({required this.icon, required this.label, required this.status});

  @override
  Widget build(BuildContext context) {
    final Color color = switch (status) {
      _StepStatus.done => AppColors.success,
      _StepStatus.active => AppColors.warning,
      _StepStatus.waiting => const Color(0xFFBDBDBD),
    };
    final IconData statusIcon = switch (status) {
      _StepStatus.done => Icons.check_circle,
      _StepStatus.active => Icons.radio_button_checked,
      _StepStatus.waiting => Icons.radio_button_unchecked,
    };

    return Row(
      children: [
        Container(
          width: 40,
          height: 40,
          decoration: BoxDecoration(
            color: color.withOpacity(0.12),
            shape: BoxShape.circle,
          ),
          child: Icon(icon, color: color, size: 20),
        ),
        const SizedBox(width: 14),
        Expanded(
          child: Text(
            label,
            style: TextStyle(
              fontSize: 14,
              fontWeight: status == _StepStatus.waiting ? FontWeight.normal : FontWeight.w600,
              color: status == _StepStatus.waiting ? const Color(0xFFBDBDBD) : const Color(0xFF212121),
            ),
          ),
        ),
        Icon(statusIcon, color: color, size: 20),
      ],
    );
  }
}

class _StepDivider extends StatelessWidget {
  final bool done;
  const _StepDivider({required this.done});

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.only(left: 19),
      child: Container(
        width: 2,
        height: 20,
        color: done ? AppColors.success.withOpacity(0.4) : const Color(0xFFE0E0E0),
      ),
    );
  }
}
