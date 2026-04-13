import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import '../../../core/constants/app_colors.dart';
import '../../../models/user_model.dart';
import '../../../routes/app_router.dart';

/// Select your role before signing up.
/// Each role tile shows icon + name + description.
class SelectRoleScreen extends ConsumerStatefulWidget {
  const SelectRoleScreen({super.key});

  @override
  ConsumerState<SelectRoleScreen> createState() => _SelectRoleScreenState();
}

class _SelectRoleScreenState extends ConsumerState<SelectRoleScreen> {
  UserRole? _selectedRole;

  static const _roleData = [
    _RoleItem(
      role: UserRole.resident,
      icon: Icons.home_outlined,
      title: 'Resident',
      subtitle: 'Flat owner / permanent resident',
      color: AppColors.resident,
    ),
    _RoleItem(
      role: UserRole.tenant,
      icon: Icons.person_outline,
      title: 'Tenant',
      subtitle: 'Renting a flat in the society',
      color: AppColors.tenant,
    ),
    _RoleItem(
      role: UserRole.securityGuard,
      icon: Icons.shield_outlined,
      title: 'Security Guard',
      subtitle: 'Manage gate entry & visitors',
      color: AppColors.guard,
    ),
    _RoleItem(
      role: UserRole.worker,
      icon: Icons.build_outlined,
      title: 'Worker',
      subtitle: 'Plumber, electrician, carpenter etc.',
      color: AppColors.worker,
    ),
    _RoleItem(
      role: UserRole.maid,
      icon: Icons.cleaning_services_outlined,
      title: 'Maid',
      subtitle: 'Housekeeping & domestic worker',
      color: AppColors.maid,
    ),
  ];

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: const Color(0xFFF5F5F5),
      appBar: AppBar(
        title: const Text('Select Your Role'),
        leading: BackButton(onPressed: () => context.pop()),
      ),
      body: Column(
        children: [
          Expanded(
            child: SingleChildScrollView(
              padding: const EdgeInsets.all(20),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  const Text(
                    'Who are you?',
                    style: TextStyle(
                      fontSize: 22,
                      fontWeight: FontWeight.bold,
                      color: Color(0xFF212121),
                    ),
                  ),
                  const SizedBox(height: 6),
                  const Text(
                    'Choose the role that best describes you',
                    style: TextStyle(fontSize: 14, color: Color(0xFF757575)),
                  ),
                  const SizedBox(height: 24),
                  ..._roleData.map((item) => _RoleTile(
                        item: item,
                        isSelected: _selectedRole == item.role,
                        onTap: () => setState(() => _selectedRole = item.role),
                      )),
                ],
              ),
            ),
          ),

          // ── Continue button
          Padding(
            padding: const EdgeInsets.fromLTRB(20, 8, 20, 28),
            child: SizedBox(
              width: double.infinity,
              height: 52,
              child: ElevatedButton(
                onPressed: _selectedRole == null
                    ? null
                    : () => context.push(
                          AppRoutes.signup,
                          extra: _selectedRole,
                        ),
                child: const Text('Continue'),
              ),
            ),
          ),
        ],
      ),
    );
  }
}

// ─── Role item data holder ─────────────────────────────────────────────────────

class _RoleItem {
  final UserRole role;
  final IconData icon;
  final String title;
  final String subtitle;
  final Color color;

  const _RoleItem({
    required this.role,
    required this.icon,
    required this.title,
    required this.subtitle,
    required this.color,
  });
}

// ─── Role tile widget ──────────────────────────────────────────────────────────

class _RoleTile extends StatelessWidget {
  final _RoleItem item;
  final bool isSelected;
  final VoidCallback onTap;

  const _RoleTile({
    required this.item,
    required this.isSelected,
    required this.onTap,
  });

  @override
  Widget build(BuildContext context) {
    return GestureDetector(
      onTap: onTap,
      child: AnimatedContainer(
        duration: const Duration(milliseconds: 200),
        margin: const EdgeInsets.only(bottom: 12),
        decoration: BoxDecoration(
          color: isSelected ? item.color.withOpacity(0.1) : Colors.white,
          borderRadius: BorderRadius.circular(14),
          border: Border.all(
            color: isSelected ? item.color : const Color(0xFFE0E0E0),
            width: isSelected ? 2 : 1,
          ),
          boxShadow: [
            if (isSelected)
              BoxShadow(
                color: item.color.withOpacity(0.2),
                blurRadius: 8,
                offset: const Offset(0, 2),
              ),
          ],
        ),
        child: Padding(
          padding: const EdgeInsets.all(16),
          child: Row(
            children: [
              Container(
                padding: const EdgeInsets.all(10),
                decoration: BoxDecoration(
                  color: item.color.withOpacity(0.15),
                  borderRadius: BorderRadius.circular(10),
                ),
                child: Icon(item.icon, color: item.color, size: 26),
              ),
              const SizedBox(width: 16),
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      item.title,
                      style: TextStyle(
                        fontSize: 16,
                        fontWeight: FontWeight.w600,
                        color: isSelected ? item.color : const Color(0xFF212121),
                      ),
                    ),
                    const SizedBox(height: 2),
                    Text(
                      item.subtitle,
                      style: const TextStyle(
                        fontSize: 13,
                        color: Color(0xFF757575),
                      ),
                    ),
                  ],
                ),
              ),
              if (isSelected)
                Icon(Icons.check_circle, color: item.color, size: 22)
              else
                const Icon(Icons.radio_button_unchecked,
                    color: Color(0xFFBDBDBD), size: 22),
            ],
          ),
        ),
      ),
    );
  }
}
