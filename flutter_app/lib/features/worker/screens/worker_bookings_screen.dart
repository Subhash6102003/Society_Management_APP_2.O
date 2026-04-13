import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../../core/constants/app_colors.dart';
import '../../../models/worker_model.dart';
import '../../../providers/data_providers.dart';
import '../../../services/database_service.dart';
import '../../../widgets/common_widgets.dart';

class WorkerBookingsScreen extends ConsumerWidget {
  const WorkerBookingsScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final ordersAsync = ref.watch(myWorkOrdersAsWorkerProvider);

    return Scaffold(
      appBar: AppBar(title: const Text('My Bookings')),
      body: ordersAsync.when(
        data: (orders) => orders.isEmpty
            ? const EmptyState(icon: Icons.assignment_outlined, title: 'No Bookings', subtitle: 'Work orders assigned to you will appear here')
            : ListView.builder(
                padding: const EdgeInsets.all(12),
                itemCount: orders.length,
                itemBuilder: (ctx, i) {
                  final o = orders[i];
                  final color = _statusColor(o.status);
                  return Card(
                    margin: const EdgeInsets.only(bottom: 10),
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        ListTile(
                          leading: CircleAvatar(
                            backgroundColor: color.withOpacity(0.12),
                            child: Icon(Icons.build, color: color),
                          ),
                          title: Text(o.title, style: const TextStyle(fontWeight: FontWeight.bold)),
                          subtitle: Column(
                            crossAxisAlignment: CrossAxisAlignment.start,
                            children: [
                              Text(o.description, maxLines: 2, overflow: TextOverflow.ellipsis),
                              if (o.flatNumber != null) Text('Flat: ${o.flatNumber}'),
                            ],
                          ),
                          trailing: StatusBadge(label: o.status.displayName, color: color),
                          isThreeLine: true,
                        ),
                        if (o.status == WorkOrderStatus.pending || o.status == WorkOrderStatus.accepted)
                          Padding(
                            padding: const EdgeInsets.fromLTRB(12, 0, 12, 12),
                            child: Row(
                              mainAxisAlignment: MainAxisAlignment.end,
                              children: [
                                if (o.status == WorkOrderStatus.pending) ...[
                                  OutlinedButton(
                                    onPressed: () => _updateStatus(context, ref, o.id, WorkOrderStatus.rejected),
                                    style: OutlinedButton.styleFrom(foregroundColor: AppColors.error),
                                    child: const Text('Reject'),
                                  ),
                                  const SizedBox(width: 8),
                                  ElevatedButton(
                                    onPressed: () => _updateStatus(context, ref, o.id, WorkOrderStatus.accepted),
                                    style: ElevatedButton.styleFrom(backgroundColor: AppColors.success),
                                    child: const Text('Accept'),
                                  ),
                                ],
                                if (o.status == WorkOrderStatus.accepted)
                                  ElevatedButton(
                                    onPressed: () => _updateStatus(context, ref, o.id, WorkOrderStatus.completed),
                                    style: ElevatedButton.styleFrom(backgroundColor: AppColors.primary),
                                    child: const Text('Mark Completed'),
                                  ),
                              ],
                            ),
                          ),
                      ],
                    ),
                  );
                },
              ),
        loading: () => const Center(child: CircularProgressIndicator()),
        error: (e, _) => ErrorState(message: e.toString()),
      ),
    );
  }

  Color _statusColor(WorkOrderStatus status) {
    switch (status) {
      case WorkOrderStatus.pending: return AppColors.warning;
      case WorkOrderStatus.accepted: return AppColors.info;
      case WorkOrderStatus.inProgress: return AppColors.info;
      case WorkOrderStatus.completed: return AppColors.success;
      case WorkOrderStatus.rejected: return AppColors.error;
      case WorkOrderStatus.cancelled: return Colors.grey;
    }
  }

  Future<void> _updateStatus(BuildContext context, WidgetRef ref, String id, WorkOrderStatus status) async {
    try {
      await DatabaseService.instance.updateWorkOrderStatus(id: id, status: status);
      ref.invalidate(myWorkOrdersAsWorkerProvider);
    } catch (e) {
      if (context.mounted) ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text('Error: $e')));
    }
  }
}
