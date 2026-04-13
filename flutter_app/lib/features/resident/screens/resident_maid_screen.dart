import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../../core/constants/app_colors.dart';
import '../../../core/utils/date_utils.dart';
import '../../../models/maid_model.dart';
import '../../../providers/auth_provider.dart';
import '../../../providers/data_providers.dart';
import '../../../services/database_service.dart';
import '../../../widgets/common_widgets.dart';

class ResidentMaidScreen extends ConsumerWidget {
  const ResidentMaidScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final maidAttendanceAsync = ref.watch(myMaidAttendanceProvider);
    final user = ref.watch(currentUserProvider);

    return Scaffold(
      appBar: AppBar(title: const Text('Maid Service')),
      body: maidAttendanceAsync.when(
        data: (attendance) => ListView(
          padding: const EdgeInsets.all(16),
          children: [
            const SectionHeader(title: 'Maid Attendance'),
            const SizedBox(height: 8),
            if (attendance.isEmpty)
              const EmptyState(icon: Icons.cleaning_services_outlined, title: 'No Records', subtitle: 'Maid attendance will appear here')
            else
              ...attendance.take(30).map((a) => Card(
                margin: const EdgeInsets.only(bottom: 8),
                child: ListTile(
                  leading: CircleAvatar(
                    backgroundColor: (a.dutyOnTime > 0 ? AppColors.success : AppColors.warning).withOpacity(0.12),
                    child: Icon(
                      a.dutyOnTime > 0 ? Icons.check_circle : Icons.pending,
                      color: a.dutyOnTime > 0 ? AppColors.success : AppColors.warning,
                    ),
                  ),
                  title: Text(a.maidName.isNotEmpty ? a.maidName : 'Maid'),
                  subtitle: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      if (a.dutyOnTime > 0) Text('Arrived: ${AppDateUtils.formatTime(DateTime.fromMillisecondsSinceEpoch(a.dutyOnTime))}'),
                      if (a.dutyOffTime > 0) Text('Left: ${AppDateUtils.formatTime(DateTime.fromMillisecondsSinceEpoch(a.dutyOffTime))}'),
                      Text(AppDateUtils.fromEpoch(a.date), style: const TextStyle(color: Colors.grey, fontSize: 11)),
                    ],
                  ),
                  isThreeLine: true,
                ),
              )).toList(),
          ],
        ),
        loading: () => const Center(child: CircularProgressIndicator()),
        error: (e, _) => ErrorState(message: e.toString()),
      ),
    );
  }
}
