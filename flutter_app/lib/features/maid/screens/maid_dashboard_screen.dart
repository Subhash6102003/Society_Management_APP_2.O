import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import '../../../core/constants/app_colors.dart';
import '../../../core/utils/date_utils.dart';
import '../../../models/user_model.dart';
import '../../../providers/auth_provider.dart';
import '../../../providers/data_providers.dart';
import '../../../routes/app_router.dart';
import '../../../services/database_service.dart';
import '../../../widgets/common_widgets.dart';

class MaidDashboardScreen extends ConsumerWidget {
  const MaidDashboardScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final user = ref.watch(currentUserProvider);
    final attendanceAsync = ref.watch(myMaidAttendanceProvider);

    return Scaffold(
      appBar: AppBar(
        title: const Text('Maid Dashboard'),
        actions: [
          IconButton(icon: const Icon(Icons.person_outline), onPressed: () => context.go(AppRoutes.maidProfile)),
          IconButton(icon: const Icon(Icons.logout), onPressed: () => ref.read(authProvider.notifier).signOut()),
        ],
      ),
      body: RefreshIndicator(
        onRefresh: () async => ref.invalidate(myMaidAttendanceProvider),
        child: attendanceAsync.when(
          data: (attendance) {
            final today = DateTime.now();
            final todayRecord = attendance.where((a) {
              final d = DateTime.fromMillisecondsSinceEpoch(a.date);
              return d.year == today.year && d.month == today.month && d.day == today.day;
            }).firstOrNull;

            final isDutyOn = todayRecord?.isDutyOn ?? false;

            return ListView(
              padding: const EdgeInsets.all(16),
              children: [
                // Welcome
                Container(
                  padding: const EdgeInsets.all(16),
                  decoration: BoxDecoration(
                    color: (user?.role ?? UserRole.maid).color,
                    borderRadius: BorderRadius.circular(16),
                  ),
                  child: Row(
                    children: [
                      UserAvatar(photoUrl: user!.photoUrl, name: user.name, radius: 28, backgroundColor: Colors.white24),
                      const SizedBox(width: 12),
                      Expanded(
                        child: Column(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            Text('Hello, ${user.name.split(' ').first}!',
                                style: const TextStyle(color: Colors.white, fontSize: 18, fontWeight: FontWeight.bold)),
                            const Text('Maid Portal', style: TextStyle(color: Colors.white70)),
                          ],
                        ),
                      ),
                    ],
                  ),
                ),
                const SizedBox(height: 20),
                // Today's duty card
                Card(
                  child: Padding(
                    padding: const EdgeInsets.all(16),
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Text("Today's Duty", style: Theme.of(context).textTheme.titleMedium?.copyWith(fontWeight: FontWeight.bold)),
                        const SizedBox(height: 12),
                        Row(
                          children: [
                            Expanded(
                              child: Column(
                                crossAxisAlignment: CrossAxisAlignment.start,
                                children: [
                                  Text(
                                    isDutyOn ? 'On Duty' : (todayRecord?.dutyOffTime != null ? 'Duty Completed' : 'Not Started'),
                                    style: TextStyle(
                                      color: isDutyOn ? AppColors.success : ((todayRecord?.dutyOffTime ?? 0) > 0 ? AppColors.info : Colors.grey),
                                      fontWeight: FontWeight.bold,
                                      fontSize: 16,
                                    ),
                                  ),
                                  if ((todayRecord?.dutyOnTime ?? 0) > 0)
                                    Text('Check-in: ${AppDateUtils.formatTime(DateTime.fromMillisecondsSinceEpoch(todayRecord!.dutyOnTime))}', style: const TextStyle(fontSize: 12, color: Colors.grey)),
                                  if ((todayRecord?.dutyOffTime ?? 0) > 0)
                                    Text('Check-out: ${AppDateUtils.formatTime(DateTime.fromMillisecondsSinceEpoch(todayRecord!.dutyOffTime))}', style: const TextStyle(fontSize: 12, color: Colors.grey)),
                                ],
                              ),
                            ),
                            if ((todayRecord?.dutyOffTime ?? 0) == 0)
                              ElevatedButton(
                                onPressed: () => _toggleDuty(context, ref, user.id, isDutyOn),
                                style: ElevatedButton.styleFrom(
                                  backgroundColor: isDutyOn ? AppColors.error : AppColors.success,
                                ),
                                child: Text(isDutyOn ? 'Check Out' : 'Check In'),
                              ),
                          ],
                        ),
                      ],
                    ),
                  ),
                ),
                const SizedBox(height: 16),
                Row(
                  mainAxisAlignment: MainAxisAlignment.spaceBetween,
                  children: [
                    const SectionHeader(title: 'Recent Attendance'),
                    TextButton(onPressed: () => context.go(AppRoutes.maidAttendance), child: const Text('View All')),
                  ],
                ),
                ...attendance.take(5).map((a) => Card(
                  margin: const EdgeInsets.only(bottom: 6),
                  child: ListTile(
                    leading: CircleAvatar(
                      backgroundColor: (a.dutyOnTime > 0 ? AppColors.success : AppColors.warning).withOpacity(0.12),
                      child: Icon(
                        a.dutyOnTime > 0 ? Icons.check : Icons.close,
                        color: a.dutyOnTime > 0 ? AppColors.success : AppColors.warning,
                        size: 18,
                      ),
                    ),
                    title: Text(AppDateUtils.fromEpoch(a.date)),
                    subtitle: Row(
                      children: [
                        if (a.dutyOnTime > 0) Text('In: ${AppDateUtils.formatTime(DateTime.fromMillisecondsSinceEpoch(a.dutyOnTime))}', style: const TextStyle(fontSize: 12)),
                        if (a.dutyOffTime > 0) Text('  Out: ${AppDateUtils.formatTime(DateTime.fromMillisecondsSinceEpoch(a.dutyOffTime))}', style: const TextStyle(fontSize: 12)),
                      ],
                    ),
                  ),
                )).toList(),
              ],
            );
          },
          loading: () => const Center(child: CircularProgressIndicator()),
          error: (e, _) => ErrorState(message: e.toString()),
        ),
      ),
    );
  }

  Future<void> _toggleDuty(BuildContext context, WidgetRef ref, String userId, bool isDutyOn) async {
    try {
      await DatabaseService.instance.toggleMaidDuty(maidId: userId, isDutyOn: isDutyOn);
      ref.invalidate(myMaidAttendanceProvider);
    } catch (e) {
      if (context.mounted) ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text('Error: $e')));
    }
  }
}
