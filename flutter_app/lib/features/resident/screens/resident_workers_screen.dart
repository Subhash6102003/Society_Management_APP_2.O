import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../../core/constants/app_colors.dart';
import '../../../models/worker_model.dart';
import '../../../providers/auth_provider.dart';
import '../../../providers/data_providers.dart';
import '../../../services/database_service.dart';
import '../../../widgets/common_widgets.dart';

class ResidentWorkersScreen extends ConsumerWidget {
  const ResidentWorkersScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final workOrdersAsync = ref.watch(myWorkOrdersAsResidentProvider);

    return Scaffold(
      appBar: AppBar(title: const Text('Workers & Services')),
      floatingActionButton: FloatingActionButton.extended(
        onPressed: () => _showBookWorkerSheet(context, ref),
        icon: const Icon(Icons.add),
        label: const Text('Book Worker'),
      ),
      body: workOrdersAsync.when(
        data: (orders) => orders.isEmpty
            ? const EmptyState(icon: Icons.build_outlined, title: 'No Work Orders', subtitle: 'Book a worker for any task')
            : ListView.builder(
                padding: const EdgeInsets.all(12),
                itemCount: orders.length,
                itemBuilder: (ctx, i) {
                  final o = orders[i];
                  final color = _statusColor(o.status);
                  return Card(
                    margin: const EdgeInsets.only(bottom: 10),
                    child: ListTile(
                      leading: CircleAvatar(
                        backgroundColor: color.withOpacity(0.12),
                        child: Icon(Icons.build, color: color),
                      ),
                      title: Text(o.title, style: const TextStyle(fontWeight: FontWeight.bold)),
                      subtitle: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Text(o.description, maxLines: 1, overflow: TextOverflow.ellipsis),
                          Text('Category: ${o.category.displayName}'),
                        ],
                      ),
                      trailing: StatusBadge(label: o.status.displayName, color: color),
                      isThreeLine: true,
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

  void _showBookWorkerSheet(BuildContext context, WidgetRef ref) {
    showModalBottomSheet(
      context: context,
      isScrollControlled: true,
      shape: const RoundedRectangleBorder(borderRadius: BorderRadius.vertical(top: Radius.circular(20))),
      builder: (ctx) => _BookWorkerSheet(onBooked: () => ref.invalidate(myWorkOrdersAsResidentProvider)),
    );
  }
}

class _BookWorkerSheet extends ConsumerStatefulWidget {
  final VoidCallback onBooked;
  const _BookWorkerSheet({required this.onBooked});

  @override
  ConsumerState<_BookWorkerSheet> createState() => _BookWorkerSheetState();
}

class _BookWorkerSheetState extends ConsumerState<_BookWorkerSheet> {
  final _formKey = GlobalKey<FormState>();
  final _titleCtrl = TextEditingController();
  final _descCtrl = TextEditingController();
  WorkerCategory _category = WorkerCategory.plumber;
  bool _loading = false;

  @override
  void dispose() {
    _titleCtrl.dispose();
    _descCtrl.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: EdgeInsets.only(left: 20, right: 20, top: 20, bottom: MediaQuery.of(context).viewInsets.bottom + 20),
      child: Form(
        key: _formKey,
        child: Column(
          mainAxisSize: MainAxisSize.min,
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text('Book a Worker', style: Theme.of(context).textTheme.titleLarge),
            const SizedBox(height: 16),
            AppTextField(
              controller: _titleCtrl,
              label: 'Task Title',
              prefixIcon: Icons.title,
              validator: (v) => v!.isEmpty ? 'Required' : null,
            ),
            const SizedBox(height: 10),
            AppTextField(
              controller: _descCtrl,
              label: 'Description',
              prefixIcon: Icons.notes,
              maxLines: 3,
              validator: (v) => v!.isEmpty ? 'Required' : null,
            ),
            const SizedBox(height: 10),
            DropdownButtonFormField<WorkerCategory>(
              value: _category,
              decoration: const InputDecoration(labelText: 'Worker Type', border: OutlineInputBorder()),
              items: WorkerCategory.values.map((c) => DropdownMenuItem(value: c, child: Text(c.displayName))).toList(),
              onChanged: (v) => setState(() => _category = v!),
            ),
            const SizedBox(height: 20),
            SizedBox(
              width: double.infinity,
              child: PrimaryButton(label: 'Request Worker', isLoading: _loading, onPressed: _submit),
            ),
          ],
        ),
      ),
    );
  }

  Future<void> _submit() async {
    if (!_formKey.currentState!.validate()) return;
    final user = ref.read(currentUserProvider);
    if (user == null) return;
    setState(() => _loading = true);
    try {
      await DatabaseService.instance.createWorkOrder(
        title: _titleCtrl.text.trim(),
        description: _descCtrl.text.trim(),
        category: _category,
        residentId: user.id,
        residentName: user.name,
        flatNumber: user.flatNumber,
      );
      widget.onBooked();
      if (mounted) Navigator.pop(context);
    } catch (e) {
      if (mounted) ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text('Error: $e')));
    } finally {
      if (mounted) setState(() => _loading = false);
    }
  }
}
