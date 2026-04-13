import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import '../../../core/constants/app_colors.dart';
import '../../../core/utils/date_utils.dart';
import '../../../providers/auth_provider.dart';
import '../../../providers/data_providers.dart';
import '../../../routes/app_router.dart';
import '../../../widgets/common_widgets.dart';

class ResidentHomeScreen extends ConsumerWidget {
  const ResidentHomeScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final user = ref.watch(currentUserProvider);
    final myBills = ref.watch(myBillsProvider);
    final myNotices = ref.watch(noticesProvider);

    return Scaffold(
      backgroundColor: AppColors.background,
      body: RefreshIndicator(
        onRefresh: () async {
          ref.invalidate(myBillsProvider);
          ref.invalidate(noticesProvider);
          ref.invalidate(myComplaintsProvider);
        },
        child: ListView(
          padding: EdgeInsets.zero,
          children: [
            // ── Header with avatar row ────────────────────────────────────────
            Container(
              color: AppColors.surface,
              padding: EdgeInsets.only(
                top: MediaQuery.of(context).padding.top + 12,
                left: 16, right: 8, bottom: 12,
              ),
              child: Row(
                children: [
                  UserAvatar(
                    photoUrl: user?.photoUrl,
                    name: user?.name ?? 'Resident',
                    radius: 22,
                  ),
                  const SizedBox(width: 12),
                  Expanded(
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Text('Hello, ${user?.name.split(' ').first ?? 'Resident'}!',
                            style: const TextStyle(fontSize: 17, fontWeight: FontWeight.bold,
                                color: AppColors.textPrimary)),
                        if (user?.flatNumber.isNotEmpty == true)
                          Text('Flat ${user!.flatNumber}${user.tower != null ? ' • Tower ${user.tower}' : ''}',
                              style: const TextStyle(color: AppColors.textSecondary, fontSize: 13)),
                      ],
                    ),
                  ),
                  IconButton(
                    icon: const Icon(Icons.notifications_outlined, color: AppColors.textSecondary),
                    onPressed: () => context.go(AppRoutes.residentNotices),
                  ),
                  IconButton(
                    icon: const Icon(Icons.person_outline, color: AppColors.textSecondary),
                    onPressed: () => context.go(AppRoutes.residentProfile),
                  ),
                ],
              ),
            ),

            Padding(
              padding: const EdgeInsets.all(16),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  // ── Pending Dues Card (primary red, matches Android) ──────
                  myBills.when(
                    data: (bills) {
                      final pending = bills.where(
                            (b) => b.status.name == 'pending' || b.status.name == 'overdue',
                      ).toList();
                      final totalDue = pending.fold<double>(0, (sum, b) => sum + b.amount);

                      return Container(
                        width: double.infinity,
                        padding: const EdgeInsets.all(20),
                        decoration: BoxDecoration(
                          color: AppColors.primary,
                          borderRadius: BorderRadius.circular(16),
                        ),
                        child: Column(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            const Text('Pending Dues',
                                style: TextStyle(color: Colors.white70, fontSize: 14)),
                            const SizedBox(height: 4),
                            Text('₹${totalDue.toStringAsFixed(0)}',
                                style: const TextStyle(
                                    color: Colors.white, fontSize: 32, fontWeight: FontWeight.bold)),
                            if (pending.isNotEmpty)
                              Padding(
                                padding: const EdgeInsets.only(top: 2),
                                child: Text(
                                  '${pending.length} bill${pending.length > 1 ? 's' : ''} pending',
                                  style: const TextStyle(color: Colors.white60, fontSize: 13),
                                ),
                              ),
                            const SizedBox(height: 16),
                            FilledButton.tonal(
                              onPressed: () => context.go(AppRoutes.residentBills),
                              style: FilledButton.styleFrom(
                                backgroundColor: Colors.white,
                                foregroundColor: AppColors.primary,
                                minimumSize: const Size(120, 40),
                                shape: RoundedRectangleBorder(
                                    borderRadius: BorderRadius.circular(10)),
                              ),
                              child: const Text('Pay Now', style: TextStyle(fontWeight: FontWeight.bold)),
                            ),
                          ],
                        ),
                      );
                    },
                    loading: () => Container(
                      width: double.infinity, height: 130,
                      decoration: BoxDecoration(
                        color: AppColors.primary, borderRadius: BorderRadius.circular(16),
                      ),
                      child: const Center(child: CircularProgressIndicator(color: Colors.white)),
                    ),
                    error: (_, __) => const SizedBox.shrink(),
                  ),

                  const SizedBox(height: 24),
                  const SectionHeader(title: 'Quick Actions'),
                  const SizedBox(height: 12),

                  // ── Quick Actions 2-column grid (matches Android) ─────────
                  GridView.count(
                    crossAxisCount: 2,
                    shrinkWrap: true,
                    physics: const NeverScrollableScrollPhysics(),
                    crossAxisSpacing: 12,
                    mainAxisSpacing: 12,
                    childAspectRatio: 1.5,
                    children: [
                      _QuickActionCard(icon: Icons.receipt_long, label: 'View Bills',
                          color: AppColors.info, onTap: () => context.go(AppRoutes.residentBills)),
                      _QuickActionCard(icon: Icons.report_problem_outlined, label: 'Complaints',
                          color: AppColors.error, onTap: () => context.go(AppRoutes.residentComplaints)),
                      _QuickActionCard(icon: Icons.groups_outlined, label: 'Add Visitor',
                          color: AppColors.success, onTap: () => context.go(AppRoutes.residentVisitors)),
                      _QuickActionCard(icon: Icons.build_outlined, label: 'Workers',
                          color: AppColors.warning, onTap: () => context.go(AppRoutes.residentWorkers)),
                      _QuickActionCard(icon: Icons.cleaning_services_outlined, label: 'Maid',
                          color: AppColors.worker, onTap: () => context.go(AppRoutes.residentMaid)),
                      _QuickActionCard(icon: Icons.campaign_outlined, label: 'Notices',
                          color: AppColors.primary, onTap: () => context.go(AppRoutes.residentNotices)),
                    ],
                  ),

                  const SizedBox(height: 24),
                  const SectionHeader(title: 'Latest Notices'),
                  const SizedBox(height: 8),
                  myNotices.when(
                    data: (notices) {
                      if (notices.isEmpty) {
                        return const Text('No notices', style: TextStyle(color: AppColors.textSecondary));
                      }
                      return Column(
                        children: notices.take(3).map((n) => Card(
                          elevation: 0,
                          margin: const EdgeInsets.only(bottom: 8),
                          shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
                          child: ListTile(
                            shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
                            leading: CircleAvatar(
                              backgroundColor: AppColors.primary.withValues(alpha: 0.12),
                              child: const Icon(Icons.campaign, color: AppColors.primary, size: 20),
                            ),
                            title: Text(n.title, style: const TextStyle(fontWeight: FontWeight.w600)),
                            subtitle: Text(n.body, maxLines: 1, overflow: TextOverflow.ellipsis),
                            trailing: const Icon(Icons.chevron_right, color: AppColors.textSecondary),
                            onTap: () => context.go(AppRoutes.residentNotices),
                          ),
                        )).toList(),
                      );
                    },
                    loading: () => const LinearProgressIndicator(),
                    error: (_, __) => const SizedBox.shrink(),
                  ),

                  const SizedBox(height: 24),
                  const SectionHeader(title: 'Pending Bills'),
                  const SizedBox(height: 8),
                  myBills.when(
                    data: (bills) {
                      final pending = bills.where((b) => b.status.name == 'pending' || b.status.name == 'overdue').take(3).toList();
                      if (pending.isEmpty) {
                        return const Text('No pending bills', style: TextStyle(color: AppColors.textSecondary));
                      }
                      return Column(
                        children: pending.map((b) => Card(
                          elevation: 0,
                          margin: const EdgeInsets.only(bottom: 8),
                          shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
                          child: ListTile(
                            shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
                            leading: CircleAvatar(
                              backgroundColor: AppColors.warning.withValues(alpha: 0.12),
                              child: const Icon(Icons.receipt_long, color: AppColors.warning, size: 20),
                            ),
                            title: Text(b.title.isNotEmpty ? b.title : b.description,
                                style: const TextStyle(fontWeight: FontWeight.w600)),
                            subtitle: Text('₹${b.amount.toStringAsFixed(0)} • Due: ${AppDateUtils.fromEpoch(b.dueDate)}'),
                            trailing: const Icon(Icons.chevron_right, color: AppColors.textSecondary),
                            onTap: () => context.go(AppRoutes.residentBills),
                          ),
                        )).toList(),
                      );
                    },
                    loading: () => const LinearProgressIndicator(),
                    error: (_, __) => const SizedBox.shrink(),
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

/// Quick action card: MaterialCard with circular icon + label (matches Android item_quick_action.xml)
class _QuickActionCard extends StatelessWidget {
  final IconData icon;
  final String label;
  final Color color;
  final VoidCallback onTap;

  const _QuickActionCard({
    required this.icon,
    required this.label,
    required this.color,
    required this.onTap,
  });

  @override
  Widget build(BuildContext context) {
    return Card(
      elevation: 1,
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
      child: InkWell(
        onTap: onTap,
        borderRadius: BorderRadius.circular(12),
        child: Padding(
          padding: const EdgeInsets.symmetric(vertical: 16, horizontal: 12),
          child: Column(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              CircleAvatar(
                backgroundColor: color.withValues(alpha: 0.15),
                radius: 22,
                child: Icon(icon, color: color, size: 22),
              ),
              const SizedBox(height: 8),
              Text(label,
                  style: TextStyle(
                      color: color, fontWeight: FontWeight.w600, fontSize: 12),
                  textAlign: TextAlign.center,
                  maxLines: 1,
                  overflow: TextOverflow.ellipsis),
            ],
          ),
        ),
      ),
    );
  }
}


