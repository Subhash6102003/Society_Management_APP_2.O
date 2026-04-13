import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import '../../../core/constants/app_colors.dart';
import '../../../providers/auth_provider.dart';
import '../../../providers/data_providers.dart';
import '../../../routes/app_router.dart';
import '../../../widgets/common_widgets.dart';

class GuardDashboardScreen extends ConsumerStatefulWidget {
  const GuardDashboardScreen({super.key});

  @override
  ConsumerState<GuardDashboardScreen> createState() => _GuardDashboardScreenState();
}

class _GuardDashboardScreenState extends ConsumerState<GuardDashboardScreen> {
  bool _onDuty = false;

  @override
  Widget build(BuildContext context) {
    final user = ref.watch(currentUserProvider);
    final allVisitors = ref.watch(visitorsProvider);

    return Scaffold(
      backgroundColor: AppColors.background,
      body: RefreshIndicator(
        onRefresh: () async => ref.invalidate(visitorsProvider),
        child: ListView(
          padding: EdgeInsets.zero,
          children: [
            // ── Header banner (guard color) ───────────────────────────────────
            Container(
              color: AppColors.guard,
              padding: EdgeInsets.only(
                top: MediaQuery.of(context).padding.top + 16,
                left: 16, right: 8, bottom: 20,
              ),
              child: Row(
                children: [
                  const CircleAvatar(
                    backgroundColor: Colors.white24,
                    radius: 24,
                    child: Icon(Icons.security, color: Colors.white, size: 28),
                  ),
                  const SizedBox(width: 12),
                  Expanded(
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Text('Welcome, ${user?.name.split(' ').first ?? 'Guard'}!',
                            style: const TextStyle(color: Colors.white, fontSize: 18, fontWeight: FontWeight.bold)),
                        const Text('Security Guard', style: TextStyle(color: Colors.white70, fontSize: 13)),
                      ],
                    ),
                  ),
                  IconButton(
                    icon: const Icon(Icons.person_outline, color: Colors.white),
                    onPressed: () => context.go(AppRoutes.guardProfile),
                  ),
                  IconButton(
                    icon: const Icon(Icons.logout, color: Colors.white),
                    onPressed: () => ref.read(authProvider.notifier).signOut(),
                  ),
                ],
              ),
            ),

            Padding(
              padding: const EdgeInsets.all(16),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  // ── Shift Toggle Card (matches Android guard_dashboard) ────
                  Card(
                    elevation: 0,
                    shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
                    child: Padding(
                      padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 14),
                      child: Row(
                        children: [
                          CircleAvatar(
                            backgroundColor: (_onDuty ? AppColors.success : AppColors.textHint)
                                .withValues(alpha: 0.15),
                            child: Icon(Icons.access_time,
                                color: _onDuty ? AppColors.success : AppColors.textHint),
                          ),
                          const SizedBox(width: 12),
                          Expanded(
                            child: Column(
                              crossAxisAlignment: CrossAxisAlignment.start,
                              children: [
                                const Text('Shift Status', style: TextStyle(fontWeight: FontWeight.w600)),
                                Text(_onDuty ? 'You are currently on duty' : 'You are off duty',
                                    style: TextStyle(
                                        fontSize: 13,
                                        color: _onDuty ? AppColors.success : AppColors.textSecondary)),
                              ],
                            ),
                          ),
                          Switch(
                            value: _onDuty,
                            onChanged: (v) => setState(() => _onDuty = v),
                            activeColor: AppColors.success,
                          ),
                        ],
                      ),
                    ),
                  ),

                  const SizedBox(height: 20),
                  const SectionHeader(title: "Today's Visitors"),
                  const SizedBox(height: 12),

                  // ── Visitor stat cards ────────────────────────────────────
                  allVisitors.when(
                    data: (visitors) {
                      final today = DateTime.now();
                      final todayVisitors = visitors.where((v) {
                        final dt = DateTime.fromMillisecondsSinceEpoch(v.visitDate);
                        return dt.year == today.year && dt.month == today.month && dt.day == today.day;
                      }).toList();
                      final pending = todayVisitors.where((v) => v.status.name == 'pending').length;
                      final checkedIn = todayVisitors.where((v) => v.status.name == 'checkedIn').length;

                      return Column(
                        children: [
                          Row(
                            children: [
                              Expanded(child: StatCard(
                                label: "Today's Visitors",
                                value: todayVisitors.length.toString(),
                                icon: Icons.groups,
                                color: AppColors.primary,
                                onTap: () => context.go(AppRoutes.guardVisitorLog),
                              )),
                              const SizedBox(width: 12),
                              Expanded(child: StatCard(
                                label: 'Pending',
                                value: pending.toString(),
                                icon: Icons.pending,
                                color: AppColors.warning,
                                onTap: () => context.go(AppRoutes.guardVisitorLog),
                              )),
                            ],
                          ),
                          const SizedBox(height: 12),
                          Row(
                            children: [
                              Expanded(child: StatCard(
                                label: 'Checked In',
                                value: checkedIn.toString(),
                                icon: Icons.login,
                                color: AppColors.success,
                              )),
                              const SizedBox(width: 12),
                              Expanded(child: StatCard(
                                label: 'Total All Time',
                                value: visitors.length.toString(),
                                icon: Icons.list_alt,
                                color: AppColors.info,
                                onTap: () => context.go(AppRoutes.guardVisitorLog),
                              )),
                            ],
                          ),
                        ],
                      );
                    },
                    loading: () => const Center(child: CircularProgressIndicator()),
                    error: (_, __) => const SizedBox.shrink(),
                  ),

                  const SizedBox(height: 24),
                  SizedBox(
                    width: double.infinity,
                    height: 52,
                    child: ElevatedButton.icon(
                      onPressed: () => context.go(AppRoutes.guardAddVisitor),
                      icon: const Icon(Icons.person_add),
                      label: const Text('Log New Visitor Entry'),
                      style: ElevatedButton.styleFrom(
                        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
                      ),
                    ),
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


