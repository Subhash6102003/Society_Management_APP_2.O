import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../../core/constants/app_colors.dart';
import '../../../models/complaint_model.dart';
import '../../../providers/auth_provider.dart';
import '../../../providers/data_providers.dart';
import '../../../services/database_service.dart';
import '../../../widgets/common_widgets.dart';

class ResidentComplaintsScreen extends ConsumerStatefulWidget {
  const ResidentComplaintsScreen({super.key});

  @override
  ConsumerState<ResidentComplaintsScreen> createState() => _ResidentComplaintsScreenState();
}

class _ResidentComplaintsScreenState extends ConsumerState<ResidentComplaintsScreen> {
  @override
  Widget build(BuildContext context) {
    final complaintsAsync = ref.watch(myComplaintsProvider);

    return Scaffold(
      appBar: AppBar(title: const Text('My Complaints')),
      floatingActionButton: FloatingActionButton.extended(
        onPressed: () => _showCreateSheet(context),
        icon: const Icon(Icons.add),
        label: const Text('Report Issue'),
      ),
      body: complaintsAsync.when(
        data: (complaints) => complaints.isEmpty
            ? const EmptyState(icon: Icons.report_problem_outlined, title: 'No Complaints', subtitle: 'Tap + to report an issue')
            : ListView.builder(
                padding: const EdgeInsets.all(12),
                itemCount: complaints.length,
                itemBuilder: (ctx, i) {
                  final c = complaints[i];
                  final color = _statusColor(c.status);
                  return Card(
                    margin: const EdgeInsets.only(bottom: 10),
                    child: ListTile(
                      leading: CircleAvatar(
                        backgroundColor: color.withOpacity(0.12),
                        child: Icon(Icons.report_problem, color: color),
                      ),
                      title: Text(c.title, style: const TextStyle(fontWeight: FontWeight.bold)),
                      subtitle: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Text(c.description, maxLines: 2, overflow: TextOverflow.ellipsis),
                          const SizedBox(height: 4),
                          Row(children: [
                            StatusBadge(label: c.status.displayName, color: color),
                            const SizedBox(width: 6),
                            StatusBadge(label: c.category.displayName, color: AppColors.info),
                          ]),
                          if (c.resolution != null) ...[
                            const SizedBox(height: 4),
                            Text('Resolution: ${c.resolution}', style: const TextStyle(color: AppColors.success, fontSize: 12)),
                          ],
                        ],
                      ),
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

  Color _statusColor(ComplaintStatus status) {
    switch (status) {
      case ComplaintStatus.open: return AppColors.warning;
      case ComplaintStatus.inProgress: return AppColors.info;
      case ComplaintStatus.resolved: return AppColors.success;
      default: return Colors.grey;
    }
  }

  void _showCreateSheet(BuildContext context) {
    showModalBottomSheet(
      context: context,
      isScrollControlled: true,
      shape: const RoundedRectangleBorder(borderRadius: BorderRadius.vertical(top: Radius.circular(20))),
      builder: (ctx) => _CreateComplaintSheet(onCreated: () => ref.invalidate(myComplaintsProvider)),
    );
  }
}

class _CreateComplaintSheet extends ConsumerStatefulWidget {
  final VoidCallback onCreated;
  const _CreateComplaintSheet({required this.onCreated});

  @override
  ConsumerState<_CreateComplaintSheet> createState() => _CreateComplaintSheetState();
}

class _CreateComplaintSheetState extends ConsumerState<_CreateComplaintSheet> {
  final _formKey = GlobalKey<FormState>();
  final _titleCtrl = TextEditingController();
  final _descCtrl = TextEditingController();
  ComplaintCategory _category = ComplaintCategory.maintenance;
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
            Text('Report Issue', style: Theme.of(context).textTheme.titleLarge),
            const SizedBox(height: 16),
            AppTextField(
              controller: _titleCtrl,
              label: 'Subject',
              prefixIcon: Icons.title,
              validator: (v) => v!.isEmpty ? 'Required' : null,
            ),
            const SizedBox(height: 10),
            AppTextField(
              controller: _descCtrl,
              label: 'Description',
              prefixIcon: Icons.notes,
              maxLines: 4,
              validator: (v) => v!.isEmpty ? 'Required' : null,
            ),
            const SizedBox(height: 10),
            DropdownButtonFormField<ComplaintCategory>(
              value: _category,
              decoration: const InputDecoration(labelText: 'Category', border: OutlineInputBorder()),
              items: ComplaintCategory.values.map((c) => DropdownMenuItem(value: c, child: Text(c.displayName))).toList(),
              onChanged: (v) => setState(() => _category = v!),
            ),
            const SizedBox(height: 20),
            SizedBox(
              width: double.infinity,
              child: PrimaryButton(label: 'Submit', isLoading: _loading, onPressed: _submit),
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
      await DatabaseService.instance.createComplaint(
        title: _titleCtrl.text.trim(),
        description: _descCtrl.text.trim(),
        category: _category,
        residentId: user.id,
        residentName: user.name,
        flatNumber: user.flatNumber,
      );
      widget.onCreated();
      if (mounted) Navigator.pop(context);
    } catch (e) {
      if (mounted) ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text('Error: $e')));
    } finally {
      if (mounted) setState(() => _loading = false);
    }
  }
}
