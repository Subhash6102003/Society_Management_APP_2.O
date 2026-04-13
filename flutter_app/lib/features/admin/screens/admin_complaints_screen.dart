import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../../core/constants/app_colors.dart';
import '../../../core/utils/date_utils.dart';
import '../../../models/complaint_model.dart';
import '../../../providers/data_providers.dart';
import '../../../services/database_service.dart';
import '../../../widgets/common_widgets.dart';

class AdminComplaintsScreen extends ConsumerStatefulWidget {
  const AdminComplaintsScreen({super.key});

  @override
  ConsumerState<AdminComplaintsScreen> createState() => _AdminComplaintsScreenState();
}

class _AdminComplaintsScreenState extends ConsumerState<AdminComplaintsScreen> {
  ComplaintStatus? _filterStatus;

  @override
  Widget build(BuildContext context) {
    final complaintsAsync = ref.watch(complaintsProvider);

    return Scaffold(
      appBar: AppBar(
        title: const Text('Complaints'),
        actions: [
          PopupMenuButton<ComplaintStatus?>(
            icon: const Icon(Icons.filter_list),
            onSelected: (v) => setState(() => _filterStatus = v),
            itemBuilder: (ctx) => [
              const PopupMenuItem(value: null, child: Text('All')),
              ...ComplaintStatus.values.map((s) => PopupMenuItem(value: s, child: Text(s.displayName))),
            ],
          ),
        ],
      ),
      body: complaintsAsync.when(
        data: (complaints) {
          final filtered = _filterStatus == null
              ? complaints
              : complaints.where((c) => c.status == _filterStatus).toList();
          if (filtered.isEmpty) return const EmptyState(icon: Icons.report_problem_outlined, title: 'No Complaints', subtitle: 'Nothing to display');
          return ListView.builder(
            padding: const EdgeInsets.all(12),
            itemCount: filtered.length,
            itemBuilder: (ctx, i) => _ComplaintCard(complaint: filtered[i], onUpdated: () => ref.invalidate(complaintsProvider)),
          );
        },
        loading: () => const Center(child: CircularProgressIndicator()),
        error: (e, _) => ErrorState(message: e.toString()),
      ),
    );
  }
}

class _ComplaintCard extends StatelessWidget {
  final ComplaintModel complaint;
  final VoidCallback onUpdated;

  const _ComplaintCard({required this.complaint, required this.onUpdated});

  @override
  Widget build(BuildContext context) {
    return Card(
      margin: const EdgeInsets.only(bottom: 10),
      child: Theme(
        data: Theme.of(context).copyWith(dividerColor: Colors.transparent),
        child: ExpansionTile(
          leading: CircleAvatar(
            backgroundColor: _statusColor.withOpacity(0.12),
            child: Icon(Icons.report_problem, color: _statusColor),
          ),
          title: Text(complaint.title, style: const TextStyle(fontWeight: FontWeight.bold)),
          subtitle: Row(children: [
            StatusBadge(label: complaint.status.displayName, color: _statusColor),
            const SizedBox(width: 6),
            StatusBadge(label: complaint.category.displayName, color: AppColors.info),
          ]),
          children: [
            Padding(
              padding: const EdgeInsets.fromLTRB(16, 0, 16, 12),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(complaint.description),
                  const SizedBox(height: 8),
                  if (complaint.userName.isNotEmpty)
                    Text('By: ${complaint.userName}', style: const TextStyle(color: Colors.grey)),
                  Text(AppDateUtils.fromEpoch(complaint.createdAt), style: const TextStyle(color: Colors.grey, fontSize: 12)),
                  if (complaint.resolution.isNotEmpty) ...[
                    const SizedBox(height: 8),
                    Container(
                      padding: const EdgeInsets.all(8),
                      decoration: BoxDecoration(color: AppColors.success.withOpacity(0.1), borderRadius: BorderRadius.circular(8)),
                      child: Text('Resolution: ${complaint.resolution}', style: const TextStyle(color: AppColors.success)),
                    ),
                  ],
                  const SizedBox(height: 12),
                  Wrap(
                    spacing: 8,
                    children: [
                      if (complaint.status == ComplaintStatus.open)
                        ElevatedButton(
                          onPressed: () => _updateStatus(context, ComplaintStatus.inProgress),
                          style: ElevatedButton.styleFrom(backgroundColor: AppColors.info),
                          child: const Text('In Progress'),
                        ),
                      if (complaint.status != ComplaintStatus.resolved)
                        ElevatedButton(
                          onPressed: () => _resolveDialog(context),
                          style: ElevatedButton.styleFrom(backgroundColor: AppColors.success),
                          child: const Text('Resolve'),
                        ),
                    ],
                  ),
                ],
              ),
            ),
          ],
        ),
      ),
    );
  }

  Color get _statusColor {
    switch (complaint.status) {
      case ComplaintStatus.open: return AppColors.warning;
      case ComplaintStatus.inProgress: return AppColors.info;
      case ComplaintStatus.resolved: return AppColors.success;
      default: return Colors.grey;
    }
  }

  Future<void> _updateStatus(BuildContext context, ComplaintStatus status, {String? resolution}) async {
    try {
      await DatabaseService.instance.updateComplaintStatus(
        id: complaint.id,
        status: status,
        resolution: resolution,
      );
      onUpdated();
    } catch (e) {
      if (context.mounted) ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text('Error: $e')));
    }
  }

  void _resolveDialog(BuildContext context) {
    final ctrl = TextEditingController();
    showDialog(
      context: context,
      builder: (ctx) => AlertDialog(
        title: const Text('Resolve Complaint'),
        content: TextField(
          controller: ctrl,
          maxLines: 3,
          decoration: const InputDecoration(hintText: 'Resolution note (optional)', border: OutlineInputBorder()),
        ),
        actions: [
          TextButton(onPressed: () => Navigator.pop(ctx), child: const Text('Cancel')),
          ElevatedButton(
            onPressed: () {
              Navigator.pop(ctx);
              _updateStatus(context, ComplaintStatus.resolved, resolution: ctrl.text.trim().isEmpty ? null : ctrl.text.trim());
            },
            child: const Text('Confirm'),
          ),
        ],
      ),
    );
  }
}
