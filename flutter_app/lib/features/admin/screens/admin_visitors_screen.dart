import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../../core/constants/app_colors.dart';
import '../../../core/utils/date_utils.dart';
import '../../../models/visitor_model.dart';
import '../../../providers/data_providers.dart';
import '../../../widgets/common_widgets.dart';

class AdminVisitorsScreen extends ConsumerWidget {
  const AdminVisitorsScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final visitorsAsync = ref.watch(visitorsProvider);

    return Scaffold(
      appBar: AppBar(title: const Text('Visitor Log')),
      body: visitorsAsync.when(
        data: (visitors) => visitors.isEmpty
            ? const EmptyState(icon: Icons.groups_outlined, title: 'No Visitors Yet', subtitle: '')
            : ListView.builder(
                padding: const EdgeInsets.all(12),
                itemCount: visitors.length,
                itemBuilder: (ctx, i) {
                  final v = visitors[i];
                  return Card(
                    margin: const EdgeInsets.only(bottom: 10),
                    child: ListTile(
                      leading: CircleAvatar(
                        backgroundImage: v.photoUrl != null ? NetworkImage(v.photoUrl!) : null,
                        child: v.photoUrl == null ? const Icon(Icons.person) : null,
                      ),
                      title: Text(v.name, style: const TextStyle(fontWeight: FontWeight.bold)),
                      subtitle: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          if (v.phoneNumber != null) Text(v.phoneNumber!),
                          if (v.flatNumber != null) Text('Flat: ${v.flatNumber}'),
                          Text('Purpose: ${v.purpose ?? '-'}'),
                          Text(AppDateUtils.fromEpoch(v.visitDate), style: const TextStyle(fontSize: 11, color: Colors.grey)),
                        ],
                      ),
                      isThreeLine: true,
                      trailing: StatusBadge(label: v.status.displayName, color: _statusColor(v.status)),
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
}
