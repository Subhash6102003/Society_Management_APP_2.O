import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../../core/constants/app_colors.dart';
import '../../../core/utils/date_utils.dart';
import '../../../models/maintenance_bill_model.dart';
import '../../../models/user_model.dart';
import '../../../providers/auth_provider.dart';
import '../../../providers/data_providers.dart';
import '../../../providers/user_provider.dart';
import '../../../services/database_service.dart';
import '../../../widgets/common_widgets.dart';

class AdminBillsScreen extends ConsumerStatefulWidget {
  const AdminBillsScreen({super.key});

  @override
  ConsumerState<AdminBillsScreen> createState() => _AdminBillsScreenState();
}

class _AdminBillsScreenState extends ConsumerState<AdminBillsScreen> {
  @override
  Widget build(BuildContext context) {
    final billsAsync = ref.watch(allBillsProvider);

    return Scaffold(
      appBar: AppBar(title: const Text('Maintenance Bills')),
      floatingActionButton: FloatingActionButton.extended(
        onPressed: () => _showCreateBillSheet(context),
        icon: const Icon(Icons.add),
        label: const Text('Create Bill'),
      ),
      body: billsAsync.when(
        data: (bills) => bills.isEmpty
            ? const EmptyState(icon: Icons.receipt_long_outlined, title: 'No Bills', subtitle: 'Tap + to create a bill')
            : ListView.builder(
                padding: const EdgeInsets.all(12),
                itemCount: bills.length,
                itemBuilder: (ctx, i) {
                  final b = bills[i];
                  return Card(
                    margin: const EdgeInsets.only(bottom: 10),
                    child: ListTile(
                      leading: CircleAvatar(
                        backgroundColor: _statusColor(b.status).withOpacity(0.12),
                        child: Icon(Icons.receipt_long, color: _statusColor(b.status)),
                      ),
                      title: Text(b.title.isNotEmpty ? b.title : b.description, style: const TextStyle(fontWeight: FontWeight.bold)),
                      subtitle: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Text('₹${b.amount.toStringAsFixed(0)}  •  Due: ${AppDateUtils.fromEpoch(b.dueDate)}'),
                          if (b.flatNumber != null) Text('Flat: ${b.flatNumber}'),
                        ],
                      ),
                      isThreeLine: true,
                      trailing: StatusBadge(label: b.status.displayName, color: _statusColor(b.status)),
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

  void _showCreateBillSheet(BuildContext context) {
    showModalBottomSheet(
      context: context,
      isScrollControlled: true,
      shape: const RoundedRectangleBorder(borderRadius: BorderRadius.vertical(top: Radius.circular(20))),
      builder: (ctx) => _CreateBillSheet(onCreated: () => ref.invalidate(allBillsProvider)),
    );
  }
}

class _CreateBillSheet extends ConsumerStatefulWidget {
  final VoidCallback onCreated;
  const _CreateBillSheet({required this.onCreated});

  @override
  ConsumerState<_CreateBillSheet> createState() => _CreateBillSheetState();
}

class _CreateBillSheetState extends ConsumerState<_CreateBillSheet> {
  final _formKey = GlobalKey<FormState>();
  final _titleCtrl = TextEditingController();
  final _amountCtrl = TextEditingController();
  final _descCtrl = TextEditingController();
  DateTime _dueDate = DateTime.now().add(const Duration(days: 30));
  String _targetType = 'all'; // 'all', 'flat'
  final _flatCtrl = TextEditingController();
  bool _loading = false;

  @override
  void dispose() {
    _titleCtrl.dispose();
    _amountCtrl.dispose();
    _descCtrl.dispose();
    _flatCtrl.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: EdgeInsets.only(
        left: 20, right: 20, top: 20,
        bottom: MediaQuery.of(context).viewInsets.bottom + 20,
      ),
      child: Form(
        key: _formKey,
        child: Column(
          mainAxisSize: MainAxisSize.min,
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text('Create Maintenance Bill', style: Theme.of(context).textTheme.titleLarge),
            const SizedBox(height: 16),
            AppTextField(
              controller: _titleCtrl,
              label: 'Title',
              prefixIcon: Icons.title,
              validator: (v) => v!.isEmpty ? 'Required' : null,
            ),
            const SizedBox(height: 10),
            AppTextField(
              controller: _amountCtrl,
              label: 'Amount (₹)',
              prefixIcon: Icons.currency_rupee,
              keyboardType: TextInputType.number,
              validator: (v) => v!.isEmpty ? 'Required' : (double.tryParse(v) == null ? 'Invalid' : null),
            ),
            const SizedBox(height: 10),
            AppTextField(
              controller: _descCtrl,
              label: 'Description (optional)',
              prefixIcon: Icons.notes,
              maxLines: 2,
            ),
            const SizedBox(height: 10),
            Row(
              children: [
                const Text('Due Date: '),
                TextButton(
                  onPressed: () async {
                    final d = await showDatePicker(
                      context: context,
                      initialDate: _dueDate,
                      firstDate: DateTime.now(),
                      lastDate: DateTime.now().add(const Duration(days: 365)),
                    );
                    if (d != null) setState(() => _dueDate = d);
                  },
                  child: Text(AppDateUtils.formatDate(_dueDate)),
                ),
              ],
            ),
            Row(
              children: [
                const Text('Target: '),
                Radio<String>(value: 'all', groupValue: _targetType, onChanged: (v) => setState(() => _targetType = v!)),
                const Text('All Flats'),
                Radio<String>(value: 'flat', groupValue: _targetType, onChanged: (v) => setState(() => _targetType = v!)),
                const Text('Specific Flat'),
              ],
            ),
            if (_targetType == 'flat') ...[
              AppTextField(
                controller: _flatCtrl,
                label: 'Flat Number',
                prefixIcon: Icons.home,
                validator: (v) => v!.isEmpty ? 'Required' : null,
              ),
              const SizedBox(height: 10),
            ],
            const SizedBox(height: 16),
            SizedBox(
              width: double.infinity,
              child: PrimaryButton(label: 'Create Bill', isLoading: _loading, onPressed: _submit),
            ),
          ],
        ),
      ),
    );
  }

  Future<void> _submit() async {
    if (!_formKey.currentState!.validate()) return;
    setState(() => _loading = true);
    try {
      if (_targetType == 'all') {
        // Get all residents/tenants and create bills for each
        final users = await DatabaseService.instance.getUsersByRole(UserRole.resident);
        final tenants = await DatabaseService.instance.getUsersByRole(UserRole.tenant);
        final allResidents = [...users, ...tenants];
        for (final user in allResidents) {
          await DatabaseService.instance.createMaintenanceBill(
            userId: user.id,
            flatNumber: user.flatNumber ?? '',
            title: _titleCtrl.text.trim(),
            amount: double.parse(_amountCtrl.text.trim()),
            dueDate: _dueDate,
            description: _descCtrl.text.trim().isEmpty ? null : _descCtrl.text.trim(),
          );
        }
      } else {
        // Need to find users in that flat - simplified: create with flat number
        await DatabaseService.instance.createMaintenanceBillForFlat(
          flatNumber: _flatCtrl.text.trim(),
          title: _titleCtrl.text.trim(),
          amount: double.parse(_amountCtrl.text.trim()),
          dueDate: _dueDate,
          description: _descCtrl.text.trim().isEmpty ? null : _descCtrl.text.trim(),
        );
      }
      widget.onCreated();
      if (mounted) Navigator.pop(context);
    } catch (e) {
      if (mounted) ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text('Error: $e')));
    } finally {
      if (mounted) setState(() => _loading = false);
    }
  }
}
