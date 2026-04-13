import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../../core/constants/app_colors.dart';
import '../../../core/utils/date_utils.dart';
import '../../../models/visitor_model.dart';
import '../../../providers/data_providers.dart';
import '../../../services/database_service.dart';
import '../../../widgets/common_widgets.dart';

class GuardVisitorLogScreen extends ConsumerWidget {
  const GuardVisitorLogScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final visitorsAsync = ref.watch(visitorsProvider);

    return Scaffold(
      appBar: AppBar(title: const Text('Visitor Log')),
      body: visitorsAsync.when(
        data: (visitors) => visitors.isEmpty
            ? const EmptyState(icon: Icons.groups_outlined, title: 'No Visitors', subtitle: '')
            : ListView.builder(
                padding: const EdgeInsets.all(12),
                itemCount: visitors.length,
                itemBuilder: (ctx, i) {
                  final v = visitors[i];
                  final color = _statusColor(v.status);
                  return Card(
                    margin: const EdgeInsets.only(bottom: 8),
                    child: ListTile(
                      leading: CircleAvatar(
                        backgroundImage: v.photoUrl != null ? NetworkImage(v.photoUrl!) : null,
                        child: v.photoUrl == null ? const Icon(Icons.person) : null,
                      ),
                      title: Text(v.name, style: const TextStyle(fontWeight: FontWeight.bold)),
                      subtitle: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          if (v.flatNumber != null) Text('Flat: ${v.flatNumber}'),
                          if (v.phoneNumber != null) Text(v.phoneNumber!),
                          Text(AppDateUtils.fromEpoch(v.visitDate), style: const TextStyle(fontSize: 11, color: Colors.grey)),
                        ],
                      ),
                      isThreeLine: true,
                      trailing: Column(
                        mainAxisSize: MainAxisSize.min,
                        children: [
                          StatusBadge(label: v.status.displayName, color: color),
                          if (v.status == VisitorStatus.approved || v.status == VisitorStatus.pending)
                            TextButton(
                              onPressed: () => _checkIn(context, ref, v.id),
                              child: const Text('Check In', style: TextStyle(fontSize: 12)),
                            ),
                          if (v.status == VisitorStatus.checkedIn)
                            TextButton(
                              onPressed: () => _checkOut(context, ref, v.id),
                              child: const Text('Check Out', style: TextStyle(fontSize: 12)),
                            ),
                        ],
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

  Color _statusColor(VisitorStatus status) {
    switch (status) {
      case VisitorStatus.approved: return AppColors.success;
      case VisitorStatus.pending: return AppColors.warning;
      case VisitorStatus.rejected: return AppColors.error;
      case VisitorStatus.checkedIn: return AppColors.info;
      case VisitorStatus.checkedOut: return Colors.grey;
      case VisitorStatus.expired: return Colors.red.shade900;
    }
  }

  Future<void> _checkIn(BuildContext context, WidgetRef ref, String visitorId) async {
    try {
      await DatabaseService.instance.updateVisitorStatus(id: visitorId, status: VisitorStatus.checkedIn);
      ref.invalidate(visitorsProvider);
    } catch (e) {
      if (context.mounted) ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text('Error: $e')));
    }
  }

  Future<void> _checkOut(BuildContext context, WidgetRef ref, String visitorId) async {
    try {
      await DatabaseService.instance.updateVisitorStatus(id: visitorId, status: VisitorStatus.checkedOut);
      ref.invalidate(visitorsProvider);
    } catch (e) {
      if (context.mounted) ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text('Error: $e')));
    }
  }
}
