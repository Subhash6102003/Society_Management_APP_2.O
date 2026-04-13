import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../../core/constants/app_colors.dart';
import '../../../models/visitor_model.dart';
import '../../../providers/auth_provider.dart';
import '../../../providers/data_providers.dart';
import '../../../services/database_service.dart';
import '../../../widgets/common_widgets.dart';

class ResidentVisitorsScreen extends ConsumerStatefulWidget {
  const ResidentVisitorsScreen({super.key});

  @override
  ConsumerState<ResidentVisitorsScreen> createState() => _ResidentVisitorsScreenState();
}

class _ResidentVisitorsScreenState extends ConsumerState<ResidentVisitorsScreen> {
  @override
  Widget build(BuildContext context) {
    final visitorsAsync = ref.watch(myVisitorsProvider);

    return Scaffold(
      appBar: AppBar(title: const Text('My Visitors')),
      floatingActionButton: FloatingActionButton.extended(
        onPressed: () => _showAddVisitorSheet(context),
        icon: const Icon(Icons.person_add),
        label: const Text('Add Visitor'),
      ),
      body: visitorsAsync.when(
        data: (visitors) => visitors.isEmpty
            ? const EmptyState(icon: Icons.groups_outlined, title: 'No Visitors', subtitle: 'Add an expected visitor')
            : ListView.builder(
                padding: const EdgeInsets.all(12),
                itemCount: visitors.length,
                itemBuilder: (ctx, i) {
                  final v = visitors[i];
                  final color = _statusColor(v.status);
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
                          if (v.purpose != null) Text('Purpose: ${v.purpose}'),
                        ],
                      ),
                      trailing: StatusBadge(label: v.status.displayName, color: color),
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

  Color _statusColor(VisitorStatus status) {
    switch (status) {
      case VisitorStatus.approved: return AppColors.success;
      case VisitorStatus.pending: return AppColors.warning;
      case VisitorStatus.rejected: return AppColors.error;
      case VisitorStatus.checkedIn: return AppColors.info;
      default: return Colors.grey;
    }
  }

  void _showAddVisitorSheet(BuildContext context) {
    showModalBottomSheet(
      context: context,
      isScrollControlled: true,
      shape: const RoundedRectangleBorder(borderRadius: BorderRadius.vertical(top: Radius.circular(20))),
      builder: (ctx) => _AddVisitorSheet(onAdded: () => ref.invalidate(myVisitorsProvider)),
    );
  }
}

class _AddVisitorSheet extends ConsumerStatefulWidget {
  final VoidCallback onAdded;
  const _AddVisitorSheet({required this.onAdded});

  @override
  ConsumerState<_AddVisitorSheet> createState() => _AddVisitorSheetState();
}

class _AddVisitorSheetState extends ConsumerState<_AddVisitorSheet> {
  final _formKey = GlobalKey<FormState>();
  final _nameCtrl = TextEditingController();
  final _phoneCtrl = TextEditingController();
  final _purposeCtrl = TextEditingController();
  bool _loading = false;

  @override
  void dispose() {
    _nameCtrl.dispose();
    _phoneCtrl.dispose();
    _purposeCtrl.dispose();
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
            Text('Add Expected Visitor', style: Theme.of(context).textTheme.titleLarge),
            const SizedBox(height: 16),
            AppTextField(
              controller: _nameCtrl,
              label: 'Visitor Name',
              prefixIcon: Icons.person,
              validator: (v) => v!.isEmpty ? 'Required' : null,
            ),
            const SizedBox(height: 10),
            AppTextField(
              controller: _phoneCtrl,
              label: 'Phone Number',
              prefixIcon: Icons.phone,
              keyboardType: TextInputType.phone,
            ),
            const SizedBox(height: 10),
            AppTextField(
              controller: _purposeCtrl,
              label: 'Purpose of Visit',
              prefixIcon: Icons.notes,
            ),
            const SizedBox(height: 20),
            SizedBox(
              width: double.infinity,
              child: PrimaryButton(label: 'Add Visitor', isLoading: _loading, onPressed: _submit),
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
      await DatabaseService.instance.createVisitor(
        residentId: user.id,
        flatNumber: user.flatNumber ?? '',
        name: _nameCtrl.text.trim(),
        phoneNumber: _phoneCtrl.text.trim().isEmpty ? null : _phoneCtrl.text.trim(),
        purpose: _purposeCtrl.text.trim().isEmpty ? null : _purposeCtrl.text.trim(),
      );
      widget.onAdded();
      if (mounted) Navigator.pop(context);
    } catch (e) {
      if (mounted) ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text('Error: $e')));
    } finally {
      if (mounted) setState(() => _loading = false);
    }
  }
}
