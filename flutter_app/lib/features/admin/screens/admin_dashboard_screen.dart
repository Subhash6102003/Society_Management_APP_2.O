import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import '../../../core/constants/app_colors.dart';
import '../../../providers/auth_provider.dart';
import '../../../providers/data_providers.dart';
import '../../../providers/user_provider.dart';
import '../../../routes/app_router.dart';
import '../../../widgets/common_widgets.dart';

class AdminDashboardScreen extends ConsumerWidget {
  const AdminDashboardScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final allUsers = ref.watch(allUsersProvider);
    final pendingUsers = ref.watch(approvalPendingUsersProvider);
    final complaints = ref.watch(complaintsProvider);
    final bills = ref.watch(allBillsProvider);
    final currentUser = ref.watch(currentUserProvider);

    return Scaffold(
      backgroundColor: AppColors.background,
      body: RefreshIndicator(
        onRefresh: () async {
          ref.invalidate(allUsersProvider);
          ref.invalidate(noticesProvider);
          ref.invalidate(complaintsProvider);
          ref.invalidate(allBillsProvider);
        },
        child: ListView(
          padding: EdgeInsets.zero,
          children: [
            // ── Red Header (matches Android fragment_admin_dashboard.xml) ─────
            Container(
              color: AppColors.primaryHeader,
              padding: EdgeInsets.only(
                top: MediaQuery.of(context).padding.top + 16,
                left: 16, right: 16, bottom: 24,
              ),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  // Top row: avatar + title + action icons
                  Row(
                    children: [
                      UserAvatar(
                        photoUrl: currentUser?.photoUrl,
                        name: currentUser?.name ?? 'Admin',
                        radius: 24,
                        backgroundColor: Colors.white24,
                      ),
                      const SizedBox(width: 12),
                      const Expanded(
                        child: Column(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            Text('Admin Dashboard',
                                style: TextStyle(color: Colors.white, fontSize: 18, fontWeight: FontWeight.bold)),
                            Text('Society Overview',
                                style: TextStyle(color: Colors.white70, fontSize: 13)),
                          ],
                        ),
                      ),
                      IconButton(
                        icon: const Icon(Icons.person_outline, color: Colors.white),
                        onPressed: () => context.go(AppRoutes.adminProfile),
                        tooltip: 'Profile',
                      ),
                      IconButton(
                        icon: const Icon(Icons.logout, color: Colors.white),
                        onPressed: () => ref.read(authProvider.notifier).signOut(),
                        tooltip: 'Logout',
                      ),
                    ],
                  ),
                  const SizedBox(height: 20),
                  // 2×2 Stat card grid inside the red header
                  GridView.count(
                    crossAxisCount: 2,
                    shrinkWrap: true,
                    physics: const NeverScrollableScrollPhysics(),
                    crossAxisSpacing: 12,
                    mainAxisSpacing: 12,
                    childAspectRatio: 1.55,
                    children: [
                      allUsers.when(
                        data: (users) => _HeaderStatCard(
                          label: 'Total Members',
                          value: users.length.toString(),
                          icon: Icons.people,
                          bgColor: AppColors.statPinkBg,
                          textColor: AppColors.statPinkText,
                          onTap: () => context.go(AppRoutes.adminUsers),
                        ),
                        loading: () => const _HeaderStatCard(label: 'Total Members', value: '...', icon: Icons.people, bgColor: AppColors.statPinkBg, textColor: AppColors.statPinkText),
                        error: (_, __) => const _HeaderStatCard(label: 'Total Members', value: '-', icon: Icons.people, bgColor: AppColors.statPinkBg, textColor: AppColors.statPinkText),
                      ),
                      pendingUsers.when(
                        data: (users) => _HeaderStatCard(
                          label: 'Pending Approval',
                          value: users.length.toString(),
                          icon: Icons.hourglass_top,
                          bgColor: const Color(0xFFFFF3E0),
                          textColor: AppColors.statOrange,
                          onTap: () => context.go(AppRoutes.adminUsers),
                        ),
                        loading: () => const _HeaderStatCard(label: 'Pending Approval', value: '...', icon: Icons.hourglass_top, bgColor: Color(0xFFFFF3E0), textColor: AppColors.statOrange),
                        error: (_, __) => const _HeaderStatCard(label: 'Pending Approval', value: '-', icon: Icons.hourglass_top, bgColor: Color(0xFFFFF3E0), textColor: AppColors.statOrange),
                      ),
                      complaints.when(
                        data: (list) {
                          final open = list.where((c) => c.status.name != 'resolved' && c.status.name != 'closed').length;
                          return _HeaderStatCard(
                            label: 'Open Complaints',
                            value: open.toString(),
                            icon: Icons.report_problem,
                            bgColor: const Color(0xFFFFEBEE),
                            textColor: AppColors.error,
                            onTap: () => context.go(AppRoutes.adminComplaints),
                          );
                        },
                        loading: () => const _HeaderStatCard(label: 'Open Complaints', value: '...', icon: Icons.report_problem, bgColor: Color(0xFFFFEBEE), textColor: AppColors.error),
                        error: (_, __) => const _HeaderStatCard(label: 'Open Complaints', value: '-', icon: Icons.report_problem, bgColor: Color(0xFFFFEBEE), textColor: AppColors.error),
                      ),
                      bills.when(
                        data: (list) {
                          final collected = list.where((b) => b.status.name == 'paid').length;
                          return _HeaderStatCard(
                            label: 'Bills Collected',
                            value: collected.toString(),
                            icon: Icons.payments,
                            bgColor: const Color(0xFFE8F5E9),
                            textColor: AppColors.statGreen,
                            onTap: () => context.go(AppRoutes.adminBills),
                          );
                        },
                        loading: () => const _HeaderStatCard(label: 'Bills Collected', value: '...', icon: Icons.payments, bgColor: Color(0xFFE8F5E9), textColor: AppColors.statGreen),
                        error: (_, __) => const _HeaderStatCard(label: 'Bills Collected', value: '-', icon: Icons.payments, bgColor: Color(0xFFE8F5E9), textColor: AppColors.statGreen),
                      ),
                    ],
                  ),
                ],
              ),
            ),

            // ── Quick Actions body ────────────────────────────────────────────
            Padding(
              padding: const EdgeInsets.all(16),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  const SectionHeader(title: 'Quick Actions'),
                  const SizedBox(height: 12),
                  _QuickActionCard(
                    icon: Icons.person_add,
                    label: 'Approve Pending Users',
                    subtitle: 'Review new registrations',
                    onTap: () => context.go(AppRoutes.adminUsers),
                    color: AppColors.warning,
                  ),
                  const SizedBox(height: 8),
                  _QuickActionCard(
                    icon: Icons.add_circle_outline,
                    label: 'Post Notice',
                    subtitle: 'Broadcast to residents',
                    onTap: () => context.go(AppRoutes.adminNotices),
                    color: AppColors.primary,
                  ),
                  const SizedBox(height: 8),
                  _QuickActionCard(
                    icon: Icons.request_quote_outlined,
                    label: 'Generate Bills',
                    subtitle: 'Create maintenance bills',
                    onTap: () => context.go(AppRoutes.adminBills),
                    color: AppColors.success,
                  ),
                  const SizedBox(height: 8),
                  _QuickActionCard(
                    icon: Icons.groups_outlined,
                    label: 'Visitor Log',
                    subtitle: 'View all society visitors',
                    onTap: () => context.go(AppRoutes.adminVisitors),
                    color: AppColors.info,
                  ),
                  const SizedBox(height: 8),
                  _QuickActionCard(
                    icon: Icons.report_problem_outlined,
                    label: 'Complaints',
                    subtitle: 'Manage resident complaints',
                    onTap: () => context.go(AppRoutes.adminComplaints),
                    color: AppColors.error,
                  ),
                ],
              ),
            ),
          ],
        ),
      ),
    );
  }
}

/// Small stat card with colored background — placed inside the red header.
class _HeaderStatCard extends StatelessWidget {
  final String label;
  final String value;
  final IconData icon;
  final Color bgColor;
  final Color textColor;
  final VoidCallback? onTap;

  const _HeaderStatCard({
    required this.label,
    required this.value,
    required this.icon,
    required this.bgColor,
    required this.textColor,
    this.onTap,
  });

  @override
  Widget build(BuildContext context) {
    return GestureDetector(
      onTap: onTap,
      child: Container(
        padding: const EdgeInsets.all(12),
        decoration: BoxDecoration(
          color: bgColor,
          borderRadius: BorderRadius.circular(12),
        ),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          mainAxisAlignment: MainAxisAlignment.spaceBetween,
          children: [
            Icon(icon, color: textColor, size: 22),
            Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(value,
                    style: TextStyle(
                      color: textColor, fontSize: 22, fontWeight: FontWeight.bold,
                    )),
                Text(label,
                    style: TextStyle(color: textColor.withValues(alpha: 0.8), fontSize: 11),
                    maxLines: 1, overflow: TextOverflow.ellipsis),
              ],
            ),
          ],
        ),
      ),
    );
  }
}

class _QuickActionCard extends StatelessWidget {
  final IconData icon;
  final String label;
  final String subtitle;
  final VoidCallback onTap;
  final Color color;

  const _QuickActionCard({
    required this.icon,
    required this.label,
    required this.subtitle,
    required this.onTap,
    required this.color,
  });

  @override
  Widget build(BuildContext context) {
    return Card(
      elevation: 0,
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
      child: ListTile(
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
        leading: CircleAvatar(
          backgroundColor: color.withValues(alpha: 0.15),
          child: Icon(icon, color: color),
        ),
        title: Text(label, style: const TextStyle(fontWeight: FontWeight.w600)),
        subtitle: Text(subtitle),
        trailing: const Icon(Icons.chevron_right),
        onTap: onTap,
      ),
    );
  }
}


