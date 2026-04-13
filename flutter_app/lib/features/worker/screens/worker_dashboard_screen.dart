import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import '../../../core/constants/app_colors.dart';
import '../../../models/worker_model.dart';
import '../../../providers/auth_provider.dart';
import '../../../providers/data_providers.dart';
import '../../../routes/app_router.dart';
import '../../../widgets/common_widgets.dart';

class WorkerDashboardScreen extends ConsumerStatefulWidget {
  const WorkerDashboardScreen({super.key});

  @override
  ConsumerState<WorkerDashboardScreen> createState() => _WorkerDashboardScreenState();
}

class _WorkerDashboardScreenState extends ConsumerState<WorkerDashboardScreen> {
  bool _onDuty = false;

  @override
  Widget build(BuildContext context) {
    final user = ref.watch(currentUserProvider);
    final workOrdersAsync = ref.watch(myWorkOrdersAsWorkerProvider);

    return Scaffold(
      backgroundColor: AppColors.background,
      body: RefreshIndicator(
        onRefresh: () async => ref.invalidate(myWorkOrdersAsWorkerProvider),
        child: workOrdersAsync.when(
          data: (orders) {
            final pending = orders.where((o) => o.status == WorkOrderStatus.pending).length;
            final active = orders.where(
                    (o) => o.status == WorkOrderStatus.accepted || o.status == WorkOrderStatus.inProgress).length;
            final done = orders.where((o) => o.status == WorkOrderStatus.completed).length;

            return ListView(
              padding: EdgeInsets.zero,
              children: [
                // ── Header banner (worker color) ──────────────────────────────
                Container(
                  color: AppColors.worker,
                  padding: EdgeInsets.only(
                    top: MediaQuery.of(context).padding.top + 16,
                    left: 16, right: 8, bottom: 20,
                  ),
                  child: Row(
                    children: [
                      UserAvatar(
                        photoUrl: user?.photoUrl,
                        name: user?.name ?? 'Worker',
                        radius: 24,
                        backgroundColor: Colors.white24,
                      ),
                      const SizedBox(width: 12),
                      Expanded(
                        child: Column(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            Text('Hello, ${user?.name.split(' ').first ?? 'Worker'}!',
                                style: const TextStyle(color: Colors.white, fontSize: 18, fontWeight: FontWeight.bold)),
                            const Text('Worker Portal', style: TextStyle(color: Colors.white70, fontSize: 13)),
                          ],
                        ),
                      ),
                      IconButton(
                        icon: const Icon(Icons.person_outline, color: Colors.white),
                        onPressed: () => context.go(AppRoutes.workerProfile),
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
                      // ── Duty Toggle Card (matches Android worker_dashboard) ─
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
                                child: Icon(Icons.work_outline,
                                    color: _onDuty ? AppColors.success : AppColors.textHint),
                              ),
                              const SizedBox(width: 12),
                              Expanded(
                                child: Column(
                                  crossAxisAlignment: CrossAxisAlignment.start,
                                  children: [
                                    const Text('Duty Status', style: TextStyle(fontWeight: FontWeight.w600)),
                                    Text(_onDuty ? 'You are currently on duty' : 'Mark yourself on duty',
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
                      const SectionHeader(title: 'Work Summary'),
                      const SizedBox(height: 12),

                      Row(
                        children: [
                          Expanded(child: StatCard(label: 'Pending', value: pending.toString(),
                              icon: Icons.pending, color: AppColors.warning,
                              onTap: () => context.go(AppRoutes.workerBookings))),
                          const SizedBox(width: 12),
                          Expanded(child: StatCard(label: 'Active', value: active.toString(),
                              icon: Icons.work, color: AppColors.info,
                              onTap: () => context.go(AppRoutes.workerBookings))),
                        ],
                      ),
                      const SizedBox(height: 12),
                      Row(
                        children: [
                          Expanded(child: StatCard(label: 'Completed', value: done.toString(),
                              icon: Icons.check_circle, color: AppColors.success,
                              onTap: () => context.go(AppRoutes.workerBookings))),
                          const SizedBox(width: 12),
                          Expanded(child: StatCard(label: 'Total Jobs', value: orders.length.toString(),
                              icon: Icons.assignment, color: AppColors.primary)),
                        ],
                      ),

                      const SizedBox(height: 20),
                      const SectionHeader(title: 'New Requests'),
                      const SizedBox(height: 8),
                      if (pending == 0)
                        const Text('No new job requests.', style: TextStyle(color: AppColors.textSecondary))
                      else
                        ...orders
                            .where((o) => o.status == WorkOrderStatus.pending)
                            .take(3)
                            .map((o) => _WorkOrderMini(
                          order: o,
                          onUpdated: () => ref.invalidate(myWorkOrdersAsWorkerProvider),
                        )),
                    ],
                  ),
                ),
              ],
            );
          },
          loading: () => const Center(child: CircularProgressIndicator()),
          error: (e, _) => ErrorState(message: e.toString()),
        ),
      ),
    );
  }
}

class _WorkOrderMini extends StatelessWidget {
  final WorkOrderModel order;
  final VoidCallback onUpdated;

  const _WorkOrderMini({required this.order, required this.onUpdated});

  @override
  Widget build(BuildContext context) {
    return Card(
      elevation: 0,
      margin: const EdgeInsets.only(bottom: 8),
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
      child: ListTile(
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
        leading: CircleAvatar(
          backgroundColor: AppColors.warning.withValues(alpha: 0.15),
          child: const Icon(Icons.work_outline, color: AppColors.warning, size: 20),
        ),
        title: Text(order.title, style: const TextStyle(fontWeight: FontWeight.bold)),
        subtitle: Text(order.description, maxLines: 1, overflow: TextOverflow.ellipsis),
        trailing: const Icon(Icons.chevron_right, color: AppColors.textSecondary),
      ),
    );
  }
}


