import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../../core/constants/app_colors.dart';
import '../../../models/worker_model.dart';
import '../../../providers/data_providers.dart';
import '../../../widgets/common_widgets.dart';

class WorkerEarningsScreen extends ConsumerWidget {
  const WorkerEarningsScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final ordersAsync = ref.watch(myWorkOrdersAsWorkerProvider);

    return Scaffold(
      appBar: AppBar(title: const Text('Earnings')),
      body: ordersAsync.when(
        data: (orders) {
          final completed = orders.where((o) => o.status == WorkOrderStatus.completed).toList();
          final totalEarned = completed.fold<double>(0, (sum, o) => sum + o.amount);

          return ListView(
            padding: const EdgeInsets.all(16),
            children: [
              Container(
                padding: const EdgeInsets.all(20),
                decoration: BoxDecoration(
                  gradient: LinearGradient(
                    colors: [AppColors.success, AppColors.success.withOpacity(0.7)],
                    begin: Alignment.topLeft,
                    end: Alignment.bottomRight,
                  ),
                  borderRadius: BorderRadius.circular(16),
                ),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    const Text('Total Earned', style: TextStyle(color: Colors.white70, fontSize: 14)),
                    const SizedBox(height: 8),
                    Text(
                      '₹${totalEarned.toStringAsFixed(0)}',
                      style: const TextStyle(color: Colors.white, fontSize: 36, fontWeight: FontWeight.bold),
                    ),
                    const SizedBox(height: 4),
                    Text('From ${completed.length} completed jobs', style: const TextStyle(color: Colors.white70)),
                  ],
                ),
              ),
              const SizedBox(height: 20),
              const SectionHeader(title: 'Completed Jobs'),
              const SizedBox(height: 8),
              if (completed.isEmpty)
                const EmptyState(icon: Icons.attach_money, title: 'No Earnings Yet', subtitle: 'Complete jobs to see your earnings')
              else
                ...completed.map((o) => Card(
                  margin: const EdgeInsets.only(bottom: 8),
                  child: ListTile(
                    leading: const CircleAvatar(
                      backgroundColor: Color(0xFFE8F5E9),
                      child: Icon(Icons.check_circle, color: AppColors.success),
                    ),
                    title: Text(o.title, style: const TextStyle(fontWeight: FontWeight.bold)),
                    subtitle: Text(o.category.displayName),
                    trailing: o.amount > 0
                        ? Text('₹${o.amount.toStringAsFixed(0)}', style: const TextStyle(fontWeight: FontWeight.bold, color: AppColors.success, fontSize: 15))
                        : const Text('—', style: TextStyle(color: Colors.grey)),
                  ),
                )).toList(),
            ],
          );
        },
        loading: () => const Center(child: CircularProgressIndicator()),
        error: (e, _) => ErrorState(message: e.toString()),
      ),
    );
  }
}
