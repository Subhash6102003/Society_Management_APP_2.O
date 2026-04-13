import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../../core/constants/app_colors.dart';
import '../../../core/utils/date_utils.dart';
import '../../../providers/data_providers.dart';
import '../../../widgets/common_widgets.dart';

class MaidAttendanceScreen extends ConsumerWidget {
  const MaidAttendanceScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final attendanceAsync = ref.watch(myMaidAttendanceProvider);

    return Scaffold(
      appBar: AppBar(title: const Text('My Attendance')),
      body: attendanceAsync.when(
        data: (attendance) => attendance.isEmpty
            ? const EmptyState(icon: Icons.fact_check_outlined, title: 'No Attendance Records', subtitle: 'Your attendance history will appear here')
            : ListView.builder(
                padding: const EdgeInsets.all(12),
                itemCount: attendance.length,
                itemBuilder: (ctx, i) {
                  final a = attendance[i];
                  final present = a.dutyOnTime > 0;
                  return Card(
                    margin: const EdgeInsets.only(bottom: 8),
                    child: ListTile(
                      leading: CircleAvatar(
                        backgroundColor: (present ? AppColors.success : AppColors.error).withOpacity(0.12),
                        child: Icon(
                          present ? Icons.check_circle : Icons.cancel,
                          color: present ? AppColors.success : AppColors.error,
                        ),
                      ),
                      title: Text(AppDateUtils.fromEpoch(a.date), style: const TextStyle(fontWeight: FontWeight.bold)),
                      subtitle: present
                          ? Row(children: [
                              Text('In: ${AppDateUtils.formatTime(DateTime.fromMillisecondsSinceEpoch(a.dutyOnTime))}', style: const TextStyle(fontSize: 12)),
                              if (a.dutyOffTime > 0)
                                Text('  Out: ${AppDateUtils.formatTime(DateTime.fromMillisecondsSinceEpoch(a.dutyOffTime))}', style: const TextStyle(fontSize: 12)),
                            ])
                          : const Text('Absent'),
                      trailing: StatusBadge(
                        label: present ? 'Present' : 'Absent',
                        color: present ? AppColors.success : AppColors.error,
                      ),
                    ),
                  );
                },
              ),
        loading: () => const Center(child: CircularProgressIndicator()),
        error: (e, _) => ErrorState(message: e.toString()),
      ),
    );
  }
}
