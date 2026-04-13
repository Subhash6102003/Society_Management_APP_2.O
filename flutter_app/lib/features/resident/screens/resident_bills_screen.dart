import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../../core/constants/app_colors.dart';
import '../../../core/utils/date_utils.dart';
import '../../../models/maintenance_bill_model.dart';
import '../../../providers/data_providers.dart';
import '../../../widgets/common_widgets.dart';

class ResidentBillsScreen extends ConsumerWidget {
  const ResidentBillsScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final billsAsync = ref.watch(myBillsProvider);

    return Scaffold(
      appBar: AppBar(title: const Text('My Bills')),
      body: billsAsync.when(
        data: (bills) => bills.isEmpty
            ? const EmptyState(icon: Icons.receipt_long_outlined, title: 'No Bills', subtitle: 'Your maintenance bills will appear here')
            : ListView.builder(
                padding: const EdgeInsets.all(12),
                itemCount: bills.length,
                itemBuilder: (ctx, i) {
                  final b = bills[i];
                  final color = _statusColor(b.status);
                  return Card(
                    margin: const EdgeInsets.only(bottom: 10),
                    child: ListTile(
                      leading: CircleAvatar(
                        backgroundColor: color.withOpacity(0.12),
                        child: Icon(Icons.receipt_long, color: color),
                      ),
                      title: Text(b.title.isNotEmpty ? b.title : b.description, style: const TextStyle(fontWeight: FontWeight.bold)),
                      subtitle: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Text('₹${b.amount.toStringAsFixed(0)}'),
                          Text('Due: ${AppDateUtils.fromEpoch(b.dueDate)}', style: const TextStyle(fontSize: 12)),
                          if (b.description != null && b.description!.isNotEmpty)
                            Text(b.description!, maxLines: 1, overflow: TextOverflow.ellipsis, style: const TextStyle(color: Colors.grey, fontSize: 12)),
                        ],
                      ),
                      isThreeLine: true,
                      trailing: StatusBadge(label: b.status.displayName, color: color),
                    ),
                  );
                },
              ),
        loading: () => const Center(child: CircularProgressIndicator()),
        error: (e, _) => ErrorState(message: e.toString()),
      ),
    );
  }

  Color _statusColor(BillStatus status) {
    switch (status) {
      case BillStatus.paid: return AppColors.success;
      case BillStatus.pending: return AppColors.warning;
      case BillStatus.overdue: return AppColors.error;
      case BillStatus.cancelled: return Colors.grey;
    }
  }
}
